package io.agora.scene.rtegame.ui.list;

import static java.lang.Boolean.TRUE;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.text.HtmlCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.agora.example.base.BaseRecyclerViewAdapter;
import io.agora.example.base.BaseUtil;
import io.agora.example.base.DividerDecoration;
import io.agora.example.base.OnItemClickListener;
import io.agora.gamesdk.GameEngine;
import io.agora.rtc2.RtcEngine;
import io.agora.scene.rtegame.BuildConfig;
import io.agora.scene.rtegame.GlobalViewModel;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.base.BaseFragment;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.databinding.GameDialogDebugBinding;
import io.agora.scene.rtegame.databinding.GameFragmentRoomListBinding;
import io.agora.scene.rtegame.databinding.GameItemRoomListBinding;
import io.agora.scene.rtegame.util.GameEnvType;
import io.agora.scene.rtegame.util.GameUtil;
import io.agora.scene.rtegame.util.ViewStatus;

public class RoomListFragment extends BaseFragment<GameFragmentRoomListBinding> implements OnItemClickListener<RoomInfo> {

    ////////////////////////////////////// -- PERMISSION --//////////////////////////////////////////////////////////////
    public static final String[] permissions = {Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA};
    ActivityResultLauncher<String[]> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), res -> {
        List<String> permissionsRefused = new ArrayList<>();
        for (String s : res.keySet()) {
            if (TRUE != res.get(s))
                permissionsRefused.add(s);
        }
        if (!permissionsRefused.isEmpty()) {
            showPermissionAlertDialog();
        } else {
            toNextPage();
        }
    });
    ////////////////////////////////////// -- VIEW MODEL --//////////////////////////////////////////////////////////////
    private RoomListViewModel mViewModel;

    ////////////////////////////////////// -- DATA --//////////////////////////////////////////////////////////////
    private BaseRecyclerViewAdapter<GameItemRoomListBinding, RoomInfo, RoomListHolder> mAdapter;
    private RoomInfo tempRoom;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GlobalViewModel.currentRoom = null;
        mViewModel = GameUtil.getViewModel(RoomListViewModel.class, this);

        initView();
        initListener();
    }

    private void initView() {
        int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;
        mAdapter = new BaseRecyclerViewAdapter<>(null, this, RoomListHolder.class);
        mBinding.recyclerViewFgList.setAdapter(mAdapter);
        mBinding.recyclerViewFgList.setLayoutManager(new GridLayoutManager(requireContext(), spanCount));
        mBinding.recyclerViewFgList.addItemDecoration(new DividerDecoration(spanCount));
        mBinding.swipeFgList.setProgressViewOffset(true, 0, mBinding.swipeFgList.getProgressViewEndOffset());
        mBinding.swipeFgList.setColorSchemeResources(R.color.game_btn_gradient_start_color, R.color.game_btn_gradient_end_color);
    }

    private void initListener() {
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.getRoot(), (v, insets) -> {
            Insets inset = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            // 顶部
            mBinding.appBarFgList.setPadding(0, inset.top, 0, 0);
            // 底部
            CoordinatorLayout.LayoutParams lpBtn = (CoordinatorLayout.LayoutParams) mBinding.btnCreateFgList.getLayoutParams();
            lpBtn.bottomMargin = inset.bottom + ((int) BaseUtil.dp2px(24));
            mBinding.btnCreateFgList.setLayoutParams(lpBtn);

            mBinding.recyclerViewFgList.setPaddingRelative(inset.left, 0, inset.right, inset.bottom);

            return WindowInsetsCompat.CONSUMED;
        });

        // "创建房间"按钮
        mBinding.btnCreateFgList.setOnClickListener((v) -> checkPermissionBeforeToNextPage(null));
        // 下拉刷新监听
        mBinding.swipeFgList.setOnRefreshListener(() -> mViewModel.fetchRoomList());
        // 状态监听
        mViewModel.viewStatus().observe(getViewLifecycleOwner(), this::onViewStatusChanged);
        // 房间列表数据监听
        mViewModel.roomList().observe(getViewLifecycleOwner(), resList -> {
            onListStatus(resList.isEmpty());
            mAdapter.submitListAndPurge(resList);
        });
//        mBinding.toolbarFgList.setOnLongClickListener(v -> showSwitchEnvDialog());

        mBinding.toolbarFgList.setOnMenuItemClickListener(item -> {
            showInfoDialog();
            return true;
        });
    }

    private void updateAvatarType(boolean isAvatar, boolean isChecked) {
        if (isAvatar) GameUtil.showAvatar = isChecked;
        else GameUtil.showNickname = isChecked;

        if (GameUtil.showAvatar) {
            if (GameUtil.showNickname) GameUtil.avatarType = 2;
            else GameUtil.avatarType = 3;
        } else {
            if (GameUtil.showNickname) GameUtil.avatarType = 1;
            else GameUtil.avatarType = 0;
        }
    }

    private void onViewStatusChanged(ViewStatus viewStatus) {
        if (viewStatus instanceof ViewStatus.Loading) {
            mBinding.swipeFgList.setRefreshing(true);
        } else if (viewStatus instanceof ViewStatus.Done)
            mBinding.swipeFgList.setRefreshing(false);
        else if (viewStatus instanceof ViewStatus.Error) {
            mBinding.swipeFgList.setRefreshing(false);
            BaseUtil.toast(requireContext(), ((ViewStatus.Error) viewStatus).msg);
        }
    }

    @Override
    public void onItemClick(@NonNull RoomInfo data, @Nullable View view, int position, long viewType) {
        checkPermissionBeforeToNextPage(data);
    }

    public void onListStatus(boolean empty) {
        mBinding.emptyViewFgList.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    /**
     * 摄像头、录音权限检查
     */
    private void checkPermissionBeforeToNextPage(@Nullable RoomInfo data) {
        tempRoom = data;

        // 小于 M 无需控制
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            toNextPage();
            return;
        }

        // 检查权限是否通过
        boolean needRequest = false;

        for (String permission : permissions) {
            if (requireContext().checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                needRequest = true;
                break;
            }
        }
        if (!needRequest) {
            toNextPage();
            return;
        }

        boolean requestDirectly = true;
        for (String requiredPermission : permissions)
            if (shouldShowRequestPermissionRationale(requiredPermission)) {
                requestDirectly = false;
                break;
            }
        // 直接申请
        if (requestDirectly) requestPermissionLauncher.launch(permissions);
            // 显示申请理由
        else showPermissionRequestDialog();
    }

    private void toNextPage() {
        GlobalViewModel.currentRoom = tempRoom;
        findNavController().navigate(R.id.action_roomListFragment_to_roomFragment);
    }

    private void showInfoDialog() {

        String htmlMessage = getString(R.string.game_info_message, BuildConfig.VERSION_NAME, GameEngine.getVersion(),
                RtcEngine.getSdkVersion());

        GameDialogDebugBinding dialogBinding = GameDialogDebugBinding.inflate(requireActivity().getLayoutInflater());

        dialogBinding.textInfoDgDebug.setText(HtmlCompat.fromHtml(htmlMessage, HtmlCompat.FROM_HTML_MODE_COMPACT));

        dialogBinding.checkWebDgDebug.setChecked(!GameUtil.usingSDKWebView);
        dialogBinding.checkWebDgDebug.setOnCheckedChangeListener((buttonView, isChecked) -> GameUtil.usingSDKWebView = !isChecked);

        dialogBinding.checkGiftDgDebug.setChecked(GameUtil.showGiftEffect);
        dialogBinding.checkGiftDgDebug.setOnCheckedChangeListener((buttonView, isChecked) -> GameUtil.showGiftEffect = isChecked);

        //    0-无头像昵称(默认），1-有昵称无头像，2-有昵称有头像，3-无眤称有头像
        dialogBinding.checkShowAvatarDgDebug.setChecked(GameUtil.showAvatar);
        dialogBinding.checkShowAvatarDgDebug.setOnCheckedChangeListener((buttonView, isChecked) -> updateAvatarType(true, isChecked));
        dialogBinding.checkShowNameDgDebug.setChecked(GameUtil.showNickname);
        dialogBinding.checkShowNameDgDebug.setOnCheckedChangeListener((buttonView, isChecked) -> updateAvatarType(false, isChecked));

        dialogBinding.btnLogDgDebug.setOnClickListener(v -> nav2LogFile());
        dialogBinding.btnLogDgDebug.setOnLongClickListener(v -> {
            BaseUtil.toast(v.getContext(), getString(R.string.game_log_toast, requireContext().getPackageName()));
            return true;
        });
        if (BuildConfig.isInternal){
            dialogBinding.rgAgoraEnv.setVisibility(View.VISIBLE);
            dialogBinding.rgHuranEnv.setVisibility(View.VISIBLE);
        }else {
            dialogBinding.rgAgoraEnv.setVisibility(View.GONE);
            dialogBinding.rgHuranEnv.setVisibility(View.GONE);
        }
        dialogBinding.rbAgoraOfficial.setChecked(GameUtil.gameEnv == GameEnvType.ENV_AGORA_OFFICIAL);
        dialogBinding.rbAgoraTest.setChecked(GameUtil.gameEnv == GameEnvType.ENV_AGORA_TEST);
        dialogBinding.rgAgoraEnv.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_agora_official) {
                    GameUtil.gameEnv = GameEnvType.ENV_AGORA_OFFICIAL;
                    BaseUtil.logD("ENV", "gameEnv:ENV_AGORA_OFFICIAL");
                } else if (checkedId == R.id.rb_agora_test) {
                    GameUtil.gameEnv = GameEnvType.ENV_AGORA_TEST;
                    BaseUtil.logD("ENV", "gameEnv:ENV_AGORA_TEST");
                }
            }
        });
        dialogBinding.rbHuranOfficial.setChecked(GameUtil.huranEnv == GameEnvType.ENV_HURAN_OFFICIAL);
        dialogBinding.rbHuranTest.setChecked(GameUtil.huranEnv == GameEnvType.ENV_HURAN_TEST);
        dialogBinding.rgHuranEnv.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_huran_official) {
                    GameUtil.huranEnv = GameEnvType.ENV_HURAN_OFFICIAL;
                    BaseUtil.logD("ENV", "gameEnv:ENV_HURAN_OFFICIAL");
                } else if (checkedId == R.id.rb_huran_test) {
                    GameUtil.huranEnv = GameEnvType.ENV_HURAN_TEST;
                    BaseUtil.logD("ENV", "gameEnv:ENV_HURAN_TEST");
                }
            }
        });

        new AlertDialog.Builder(requireContext()).setTitle(R.string.game_info)
                .setView(dialogBinding.getRoot())
                .show();
    }

    private void showPermissionAlertDialog() {
        new AlertDialog.Builder(requireContext()).setMessage(R.string.game_permission_refused)
                .setPositiveButton(android.R.string.ok, null).show();
    }

    private void showPermissionRequestDialog() {
        new AlertDialog.Builder(requireContext()).setMessage(R.string.game_permission_alert).setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, ((dialogInterface, i) -> requestPermissionLauncher.launch(permissions))).show();
    }

//    private boolean showSwitchEnvDialog() {
//        new android.app.AlertDialog.Builder(requireContext()).setTitle(R.string.game_set_game_env)
//                .setSingleChoiceItems(new String[]{getString(R.string.game_official_env),
//                        getString(R.string.game_test_env),}, GameUtil.gameEnv, (dialog, which) -> GameUtil.gameEnv = which)
//                .setPositiveButton(android.R.string.ok, null).show();
//        return true;
//    }

    private void nav2LogFile() {
        File cacheDir = new File(requireActivity().getExternalCacheDir(), "AGORA_GAME_SDK.log");
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri dirUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", cacheDir);
        intent.setDataAndType(dirUri, "text/plain");
        Intent.createChooser(intent, "Open Log");

        startActivity(intent);
    }
}
