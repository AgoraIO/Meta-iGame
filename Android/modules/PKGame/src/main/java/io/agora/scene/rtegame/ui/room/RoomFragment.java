package io.agora.scene.rtegame.ui.room;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.Spanned;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.scene.rtegame.GlobalViewModel;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.base.BaseFragment;
import io.agora.scene.rtegame.bean.GameApplyInfo;
import io.agora.scene.rtegame.bean.GameInfo;
import io.agora.scene.rtegame.bean.GiftInfo;
import io.agora.scene.rtegame.bean.PKApplyInfo;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.databinding.GameFragmentRoomBinding;
import io.agora.scene.rtegame.databinding.GameItemRoomMessageBinding;
import io.agora.scene.rtegame.ui.room.donate.DonateDialog;
import io.agora.scene.rtegame.ui.room.game.GameListDialog;
import io.agora.scene.rtegame.ui.room.tool.MoreDialog;
import io.agora.scene.rtegame.util.EventObserver;
import io.agora.scene.rtegame.util.GameUtil;
import io.agora.scene.rtegame.util.GiftUtil;
import io.agora.scene.rtegame.util.ViewStatus;
import io.agora.scene.rtegame.view.LiveHostCardView;
import io.agora.scene.rtegame.view.LiveHostLayout;

public class RoomFragment extends BaseFragment<GameFragmentRoomBinding> {

    private RoomViewModel mViewModel;

    private BaseRecyclerViewAdapter<GameItemRoomMessageBinding, CharSequence, MessageHolder> mMessageAdapter;

    private RoomInfo currentRoom;
    //  See if current user is the host
    private boolean aMHost;
    private boolean shouldShowInputBox = false;
    private AlertDialog currentDialog;

    private final String preLoadedURL = "file:///android_asset/index.html";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (GlobalViewModel.rtcEngine == null || GlobalViewModel.localUser == null) {
            findNavController().popBackStack();
            return null;
        }

        currentRoom = GlobalViewModel.currentRoom;
        if (currentRoom == null) {
            findNavController().navigate(R.id.action_roomFragment_to_roomCreateFragment);
            return null;
        }

        mViewModel = GameUtil.getViewModel(RoomViewModel.class, new RoomViewModelFactory(currentRoom, GlobalViewModel.localUser, GlobalViewModel.rtcEngine), this);
        aMHost = mViewModel.amHost;

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
        initListener();
        onRTCInit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mViewModel != null)
            mViewModel.stopLocalPreview();
    }

    private void initView() {
        Glide.with(this).load(GameUtil.getBgdByRoomBgdId(currentRoom.getBackgroundId()))
                .centerCrop().into(mBinding.layoutRoomInfo.avatarHostFgRoom);
        mBinding.layoutRoomInfo.nameHostFgRoom.setText(getString(R.string.game_format_two_string, currentRoom.getRoomName(), currentRoom.getId()));

        // config game view
        new Handler(Looper.getMainLooper()).post(() -> {
            if (mBinding != null) {

                int dp12 = (int) BaseUtil.dp2px(12);
                // 设置 Game View 距离顶部距离 (+ 12dp)
                int topMargin = mBinding.layoutRoomInfo.getRoot().getBottom() + dp12;
                // Game Mode 下摄像头预览距离底部距离
                int marginBottom = mBinding.getRoot().getMeasuredHeight() - mBinding.btnExitFgRoom.getTop() + dp12;
                // Game View 高度
                int height = mBinding.btnExitFgRoom.getTop() - mBinding.recyclerViewFgRoom.getMeasuredHeight() - dp12 * 2 - topMargin;
                mBinding.hostContainerFgRoom.initParams(topMargin, height, marginBottom, mBinding.recyclerViewFgRoom.getPaddingLeft());
                FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mBinding.gameContainerFgRoom.getLayoutParams();
                layoutParams.bottomMargin = marginBottom + mBinding.recyclerViewFgRoom.getMeasuredHeight();
                layoutParams.topMargin = topMargin;
                mBinding.gameContainerFgRoom.setLayoutParams(layoutParams);
            }
        });

        mMessageAdapter = new BaseRecyclerViewAdapter<>(null, MessageHolder.class);
        mBinding.recyclerViewFgRoom.setAdapter(mMessageAdapter);
        // Android 12 over_scroll animation is phenomenon
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
            mBinding.recyclerViewFgRoom.setOverScrollMode(View.OVER_SCROLL_NEVER);

        hideBtnByCurrentRole();
    }

    private void hideBtnByCurrentRole() {
        mBinding.btnGameFgRoom.setVisibility(aMHost ? View.VISIBLE : View.GONE);
        mBinding.btnMoreFgRoom.setVisibility(aMHost ? View.VISIBLE : View.GONE);
        mBinding.btnDonateFgRoom.setVisibility(aMHost ? View.GONE : View.VISIBLE);
    }

    private void initListener() {
        handleWindowInset();


        // 本地消息
        mBinding.editTextFgRoom.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String msg = v.getText().toString().trim();
                if (!msg.isEmpty())
                    insertUserMessage(msg);
                BaseUtil.hideKeyboardCompat(v);
                v.setText("");
            }
            return false;
        });
        // "更多"弹窗
        mBinding.btnMoreFgRoom.setOnClickListener(v -> {
            new MoreDialog().setLocalVideoStreamListener((button, isMute) -> {
                LiveHostCardView hostView = mBinding.hostContainerFgRoom.hostView;
                if (hostView==null) return;
                if (isMute) {
                    hostView.setVisibility(View.INVISIBLE);
                } else {
                    hostView.setVisibility(View.VISIBLE);
                }
            }).show(getChildFragmentManager(), MoreDialog.TAG);
        });
//        mBinding.btnMoreFgRoom.setOnClickListener(v -> GameEngine.getInstance().setOption(2, GameSetOptions.GAME_STATE, "{\"state\":\"app_common_self_ready\",\"data\":{\"isReady\":true}}"));
        // "游戏"弹窗
        mBinding.btnGameFgRoom.setOnClickListener(v -> new GameListDialog().show(getChildFragmentManager(), GameListDialog.TAG));
        // "退出游戏"按钮
        mBinding.btnExitGameFgRoom.setOnClickListener(v -> mViewModel.requestExitGame());
        // "终止连麦"按钮
        mBinding.btnExitPkFgRoom.setOnClickListener(v -> mViewModel.endPK());
        // "礼物"弹窗
        mBinding.btnDonateFgRoom.setOnClickListener(v -> new DonateDialog().show(getChildFragmentManager(), DonateDialog.TAG));
        // "退出直播间"按钮点击事件
        mBinding.btnExitFgRoom.setOnClickListener(v -> { mViewModel.exitGame(); requireActivity().onBackPressed();});
        mBinding.editTextFgRoom.setShowSoftInputOnFocus(true);
        // 显示键盘按钮
        mBinding.inputFgRoom.setOnClickListener(v -> {
            shouldShowInputBox = true;
            BaseUtil.showKeyboardCompat(mBinding.inputLayoutFgRoom);
        });
        // 连麦成功《==》主播上线
        mViewModel.subRoomInfo().observe(getViewLifecycleOwner(), this::onSubHostJoin);
        // 礼物监听
        mViewModel.gift().observe(getViewLifecycleOwner(), new EventObserver<>(this::onGiftUpdated));

        // 主播，监听连麦信息
        if (aMHost) {
            // 游戏开始
            mViewModel.applyInfo().observe(getViewLifecycleOwner(), this::onPKApplyInfoChanged);
            mViewModel.currentGame().observe(getViewLifecycleOwner(), this::onGameInfoChanged);
        } else {
            mViewModel.gameShareInfo().observe(getViewLifecycleOwner(), this::onGameShareInfoChanged);
        }

        mViewModel.viewStatus().observe(getViewLifecycleOwner(), viewStatus -> {
            if (viewStatus instanceof ViewStatus.Message)
                insertNewMessage(((ViewStatus.Message) viewStatus).msg);
            else if (viewStatus instanceof ViewStatus.Error) {
                BaseUtil.toast(requireContext(), ((ViewStatus.Error) viewStatus).msg);
                findNavController().popBackStack();
            } else if (viewStatus instanceof ViewStatus.Done) {
                View gameView = mViewModel.getGameView();
                if (gameView != null) {
                    mBinding.gameContainerFgRoom.addView(gameView);
                }
            } else if (viewStatus instanceof ViewStatus.Update){
                mViewModel.updateGameCode(getString(R.string.game_sud_app_id));
            }
        });
    }

    //<editor-fold desc="邀请 相关">
    private void onPKApplyInfoChanged(PKApplyInfo pkApplyInfo) {
        if (currentDialog != null) currentDialog.dismiss();
        if (pkApplyInfo == null) return;
        if (pkApplyInfo.getStatus() == PKApplyInfo.APPLYING) {
            showPKDialog(pkApplyInfo);
        } else if (pkApplyInfo.getStatus() == PKApplyInfo.REFUSED) {
            insertNewMessage("邀请已拒绝");
        }
    }

    /**
     * 仅主播调用
     */
    private void showPKDialog(PKApplyInfo pkApplyInfo) {
        if (pkApplyInfo.getRoomId().equals(currentRoom.getId())) {
            insertNewMessage(getString(R.string.game_ready_message));
            currentDialog = new AlertDialog.Builder(requireContext()).setMessage(R.string.game_ready_message).setCancelable(false)
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        mViewModel.cancelApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    }).show();
        } else {
            currentDialog = new AlertDialog.Builder(requireContext()).setMessage(getString(R.string.game_accept_message, pkApplyInfo.getUserName())).setCancelable(false)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                        mViewModel.acceptApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    })
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        mViewModel.cancelApplyPK(pkApplyInfo);
                        dialog.dismiss();
                    }).show();
        }
    }
    //</editor-fold>

    //<editor-fold desc="游戏相关">
    private void onGameInfoChanged(GameApplyInfo currentGame) {
        if (currentGame == null) return;
        BaseUtil.logD("game status->" + currentGame.getStatus());
        if (!aMHost) return;

        if (currentGame.getStatus() == GameApplyInfo.PLAYING) {
            onLayoutTypeChanged(LiveHostLayout.Type.DOUBLE_IN_GAME);
            startGame(currentGame.getVendorId());
        } else if (currentGame.getStatus() == GameApplyInfo.END) {
            onGameStoppedForCustomWebView();
            mViewModel.exitGame();
            onLayoutTypeChanged(atMost(LiveHostLayout.Type.DOUBLE));
        }
    }

    /**
     * 仅观众调用
     *
     * @param gameInfo
     */
    private void onGameShareInfoChanged(GameInfo gameInfo) {
        if (gameInfo == null) return;
        if (gameInfo.getStatus() == GameInfo.START) {
            insertNewMessage("加载远端游戏画面");
            onLayoutTypeChanged(LiveHostLayout.Type.DOUBLE_IN_GAME);
            startGame(gameInfo.getVendorId());
        } else if (gameInfo.getStatus() == GameInfo.END) {
            insertNewMessage("停止远端游戏画面");
            onGameStoppedForCustomWebView();
            mViewModel.exitGame();
            onLayoutTypeChanged(atMost(LiveHostLayout.Type.DOUBLE));
        }
    }
    //</editor-fold>

    /**
     * 主播 || 自己送的==》消息提示
     * 不是主播 ==》显示特效
     */
    private void onGiftUpdated(GiftInfo giftInfo) {
        if (giftInfo == null) return;



        String giftDesc = GiftUtil.getGiftDesc(requireContext(), giftInfo);
        if (giftDesc != null) insertNewMessage(giftDesc);
        // 屏蔽礼物动效
//        if (!aMHost && GameUtil.showGiftEffect) {
//            mBinding.giftImageFgRoom.setVisibility(View.VISIBLE);
//            int giftId = GiftUtil.getGiftIdFromGiftInfo(requireContext(), giftInfo);
//            Glide.with(this).asGif().load(GiftUtil.getGifByGiftId(giftId))
//                    .listener(new RequestListener<GifDrawable>() {
//                        @Override
//                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target, boolean isFirstResource) {
//                            mBinding.giftImageFgRoom.setVisibility(View.GONE);
//                            return false;
//                        }
//
//                        @Override
//                        public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource, boolean isFirstResource) {
//                            resource.setLoopCount(1);
//                            resource.registerAnimationCallback(new Animatable2Compat.AnimationCallback() {
//                                @Override
//                                public void onAnimationEnd(Drawable drawable) {
//                                    super.onAnimationEnd(drawable);
//                                    mBinding.giftImageFgRoom.setVisibility(View.GONE);
//                                }
//                            });
//                            return false;
//                        }
//                    })
//                    .into(mBinding.giftImageFgRoom);
//        }
    }

    /**
     * RTC 初始化成功
     */
    private void onRTCInit() {
        BaseUtil.logD("onRTCInit");
        // 如果是房主，创建View开始直播
        if (aMHost) initLocalPreview();
        else initRemotePreview();
    }

    /**
     * 本地 View 初始化
     * 仅主播本人调用
     */
    @MainThread
    private void initLocalPreview() {
        LiveHostCardView view = mBinding.hostContainerFgRoom.createHostView();
        onLayoutTypeChanged(atLeast(LiveHostLayout.Type.HOST_ONLY));
        mViewModel.setupLocalPreview(view.renderTextureView);
        insertNewMessage("画面加载完成");
    }

    /**
     * 主播上线
     */
    @MainThread
    private void initRemotePreview() {
        insertNewMessage("正在加载主播【" + currentRoom.getTempUserName() + "】视频");
        LiveHostLayout liveHost = mBinding.hostContainerFgRoom;

        LiveHostCardView view = liveHost.createHostView();
        onLayoutTypeChanged(atLeast(LiveHostLayout.Type.HOST_ONLY));
        mViewModel.setupRemotePreview(view.renderTextureView, currentRoom, true);
    }

    /**
     * 连麦主播上线
     */
    @MainThread
    private void onSubHostJoin(@Nullable RoomInfo subRoomInfo) {
        LiveHostLayout container = mBinding.hostContainerFgRoom;
        // remove subHostView
        if (subRoomInfo == null) {
            insertNewMessage("连麦结束");
            onLayoutTypeChanged(LiveHostLayout.Type.HOST_ONLY);
        } else {
            insertNewMessage("正在加载连麦主播【" + subRoomInfo.getTempUserName() + "】视频");
            LiveHostCardView view = container.createSubHostView();

            onLayoutTypeChanged(atLeast(LiveHostLayout.Type.DOUBLE));
            mViewModel.setupRemotePreview(view.renderTextureView, subRoomInfo, false);
        }
    }

    /**
     * 用户发送消息
     * 直播间滚动消息
     */
    private void insertUserMessage(String msg) {
        Spanned userMessage = Html.fromHtml(getString(R.string.game_user_msg, mViewModel.localUser.getName(), msg));
        insertNewMessage(userMessage);
    }

    /**
     * 直播间滚动消息
     */
    private void insertNewMessage(CharSequence msg) {
        mMessageAdapter.addItem(msg);
        int count = mMessageAdapter.getItemCount();
        if (count > 0)
            mBinding.recyclerViewFgRoom.smoothScrollToPosition(count - 1);
    }

    private void onLayoutTypeChanged(LiveHostLayout.Type type) {
        BaseUtil.logD("onLayoutTypeChanged:" + type.ordinal());
        mBinding.hostContainerFgRoom.setType(type);

//        确保游戏正常结束
        if (type != LiveHostLayout.Type.DOUBLE_IN_GAME)
            mViewModel.exitGame();

        if (aMHost) {
            if (type == LiveHostLayout.Type.HOST_ONLY) {
                mBinding.btnGameFgRoom.setVisibility(View.VISIBLE);
                mBinding.btnExitPkFgRoom.setVisibility(View.GONE);
                mBinding.btnExitGameFgRoom.setVisibility(View.GONE);
            } else if (type == LiveHostLayout.Type.DOUBLE) {
                mBinding.btnGameFgRoom.setVisibility(View.VISIBLE);
                mBinding.btnExitGameFgRoom.setVisibility(View.GONE);
                mBinding.btnExitPkFgRoom.setVisibility(View.VISIBLE);
            } else {
                mBinding.btnGameFgRoom.setVisibility(View.GONE);
                mBinding.btnExitGameFgRoom.setVisibility(View.VISIBLE);
                mBinding.btnExitPkFgRoom.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 至少在这个状态
     * <p>
     * 连麦主播上线
     * 在游戏 {@link LiveHostLayout.Type#DOUBLE_IN_GAME}
     * 不在游戏 {@link LiveHostLayout.Type#DOUBLE}
     */
    private LiveHostLayout.Type atLeast(@NonNull LiveHostLayout.Type type) {
        if (type.ordinal() < mBinding.hostContainerFgRoom.getType().ordinal())
            return mBinding.hostContainerFgRoom.getType();
        else return type;
    }

    /**
     * 不能超过这个状态
     * 游戏结束
     * 在连麦 {@link LiveHostLayout.Type#DOUBLE}
     * 不在连麦 {@link LiveHostLayout.Type#HOST_ONLY}
     */
    private LiveHostLayout.Type atMost(@NonNull LiveHostLayout.Type type) {
        if (type.ordinal() > mBinding.hostContainerFgRoom.getType().ordinal())
            return mBinding.hostContainerFgRoom.getType();
        else return type;
    }

    private void handleWindowInset() {
        // 沉浸处理
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 整体留白
            mBinding.containerOverlayFgRoom.setPadding(inset.left, inset.top, inset.right, inset.bottom);
            // 输入框显隐及位置偏移
            boolean imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime());
            if (imeVisible) {
                if (shouldShowInputBox) {
                    mBinding.inputLayoutFgRoom.setVisibility(View.VISIBLE);
                    mBinding.inputLayoutFgRoom.requestFocus();
                    int desiredY = -insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
                    mBinding.inputLayoutFgRoom.setTranslationY(desiredY);
                }
            } else {
                shouldShowInputBox = false;
                if (mBinding.inputLayoutFgRoom.getVisibility() == View.VISIBLE) {
                    mBinding.inputLayoutFgRoom.setVisibility(View.GONE);
                    mBinding.inputLayoutFgRoom.clearFocus();
                }
            }
            return WindowInsetsCompat.CONSUMED;
        });
    }

    private Rect getSafePaddingAsRect() {
        Rect rect = new Rect();
//        rect.set(0, mBinding.layoutRoomInfo.getRoot().getBottom(), 0, mBinding.getRoot().getMeasuredHeight() - mBinding.recyclerViewFgRoom.getTop());
        return rect;
    }

    private void startGame(String vendorId) {
        if (GameUtil.isSud(vendorId)) {
            // 忽然
            mViewModel.getGameCode(getString(R.string.game_sud_app_id), new RoomViewModel.GameGetCodeListener() {
                @Override
                public void onSuccess(String code) {
                    Activity activity = getActivity();
                    if (activity != null) {
                        mViewModel.startSudGame(mBinding.gameContainerFgRoom, code, getString(R.string.game_sud_app_id), getString(R.string.game_sdu_app_key), getSafePaddingAsRect());
                    }
                }

                @Override
                public void onFailed() {}
            });
        } else {
            if (GameUtil.usingSDKWebView)
                mViewModel.startGame(mBinding.gameContainerFgRoom, null, getSafePaddingAsRect());
            else {
                WebView webView = generateWebView(requireContext());
                mBinding.gameContainerFgRoom.addView(webView);
                mViewModel.startGame(null, webView, getSafePaddingAsRect());
            }
        }
    }

    private void onGameStoppedForCustomWebView() {
        mBinding.gameContainerFgRoom.removeAllViews();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private WebView generateWebView(Context context) {
        WebView webView = new WebView(context);
        webView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl(preLoadedURL);
        return webView;
    }
}
