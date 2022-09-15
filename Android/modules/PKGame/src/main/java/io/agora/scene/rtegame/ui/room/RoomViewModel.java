package io.agora.scene.rtegame.ui.room;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

import io.agora.example.base.BaseUtil;
import io.agora.gamesdk.GameContext;
import io.agora.gamesdk.GameEngine;
import io.agora.gamesdk.GameOptions;
import io.agora.gamesdk.IGameEngineEventHandler;
import io.agora.gamesdk.LogConfig;
import io.agora.gamesdk.annotations.GameGetOptions;
import io.agora.gamesdk.annotations.GameSetOptions;
import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcConnection;
import io.agora.rtc2.RtcEngineEx;
import io.agora.rtc2.internal.RtcEngineImpl;
import io.agora.rtc2.video.VideoCanvas;
import io.agora.scene.rtegame.GlobalViewModel;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.api.GameHttpServer;
import io.agora.scene.rtegame.bean.AgoraGame;
import io.agora.scene.rtegame.bean.GameApplyInfo;
import io.agora.scene.rtegame.bean.GameInfo;
import io.agora.scene.rtegame.bean.GiftInfo;
import io.agora.scene.rtegame.bean.LocalUser;
import io.agora.scene.rtegame.bean.PKApplyInfo;
import io.agora.scene.rtegame.bean.PKInfo;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.bean.api.SudLoginBean;
import io.agora.scene.rtegame.bean.sdk.AgoraGameList;
import io.agora.scene.rtegame.bean.sdk.FetchGameListRequiredBean;
import io.agora.scene.rtegame.bean.sdk.JoinGameRequiredBean;
import io.agora.scene.rtegame.bean.sdk.SendGiftRequiredBean;
import io.agora.scene.rtegame.bean.sdk.SudAdditionalRequired;
import io.agora.scene.rtegame.bean.sdk.SudGameMessage;
import io.agora.scene.rtegame.bean.sdk.SudJoinGameRequiredBean;
import io.agora.scene.rtegame.util.Event;
import io.agora.scene.rtegame.util.GamSyncEventListener;
import io.agora.scene.rtegame.util.GameConstants;
import io.agora.scene.rtegame.util.GameEnvType;
import io.agora.scene.rtegame.util.GameUtil;
import io.agora.scene.rtegame.util.GsonTool;
import io.agora.scene.rtegame.util.ViewStatus;
import io.agora.syncmanager.rtm.IObject;
import io.agora.syncmanager.rtm.SceneReference;
import io.agora.syncmanager.rtm.Sync;
import io.agora.syncmanager.rtm.SyncManagerException;


/**
 * @author lq
 */
@Keep
public class RoomViewModel extends ViewModel {

    //<editor-fold desc="Persistent variable">
    public final RoomInfo currentRoom;
    private final RtcEngineEx rtcEngineEx;
    @Nullable
    public AgoraGame roomGame;
    @NonNull
    public final LocalUser localUser;
    public final boolean amHost;
    @Nullable
    public SceneReference currentSceneRef = null;
    @Nullable
    private SceneReference targetSceneRef = null;

    public boolean isLocalVideoMuted = false;
    public boolean isLocalMicMuted = false;

    //</editor-fold>


    //<editor-fold desc="Live data">
    private final MutableLiveData<List<AgoraGame>> _gameList = new MutableLiveData<>(new ArrayList<>());

    @NonNull
    public LiveData<List<AgoraGame>> gameList() {
        return _gameList;
    }

    // UI状态
    private final MutableLiveData<ViewStatus> _viewStatus = new MutableLiveData<>();

    @NonNull
    public LiveData<ViewStatus> viewStatus() {
        return _viewStatus;
    }

    // 直播间礼物信息
    private final MutableLiveData<Event<GiftInfo>> _gift = new MutableLiveData<>(new Event<>(null));

    @NonNull
    public LiveData<Event<GiftInfo>> gift() {
        return _gift;
    }

    // 连麦房间信息
    private final MutableLiveData<RoomInfo> _subRoomInfo = new MutableLiveData<>();

    @NonNull
    public LiveData<RoomInfo> subRoomInfo() {
        return _subRoomInfo;
    }

    // 当前在玩游戏信息
    private final MutableLiveData<GameApplyInfo> _currentGame = new MutableLiveData<>();

    @NonNull
    public LiveData<GameApplyInfo> currentGame() {
        return _currentGame;
    }

    // 连麦房间信息
    private final MutableLiveData<GameInfo> _gameShareInfo = new MutableLiveData<>();

    @NonNull
    public LiveData<GameInfo> gameShareInfo() {
        return _gameShareInfo;
    }

    private final MutableLiveData<PKApplyInfo> _applyInfo = new MutableLiveData<>();

    @NonNull
    public LiveData<PKApplyInfo> applyInfo() {
        return _applyInfo;
    }

    private final MutableLiveData<Event<Boolean>> _pkResult = new MutableLiveData<>();

    @NonNull
    public LiveData<Event<Boolean>> pkResult() {
        return _pkResult;
    }
    //</editor-fold>


    private volatile RoomInfo tempSubRoom = null;

    private final int GAME_LIMIT = 10;
    private int currentPage = 1;
    private int totalPage = 1;

    private final IRtcEngineEventHandler mySubEventHandler = new IRtcEngineEventHandler() {
        @Override
        public void onJoinChannelSuccess(String channel, int uid, int elapsed) {
            super.onJoinChannelSuccess(channel, uid, elapsed);
            BaseUtil.logD("onJoinChannelSuccess:" + channel + uid);
            if (tempSubRoom != null)
                _subRoomInfo.postValue(tempSubRoom);
        }
    };

    //<editor-fold desc="Init and end">
    public RoomViewModel(@NonNull RoomInfo roomInfo, @NonNull LocalUser localUser, @NonNull RtcEngineEx rtcEngineEx) {
        this.currentRoom = roomInfo;
        this.localUser = localUser;
        this.rtcEngineEx = rtcEngineEx;
        this.amHost = Objects.equals(currentRoom.getUserId(), localUser.getUserId());

        configRTC(rtcEngineEx);
        configRTM();
        configGameEngine();
        joinRoom();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        BaseUtil.logD("RoomViewModel onCleared");
        rtcEngineEx.removeHandler(mySubEventHandler);
        rtcEngineEx.leaveChannel();
        if (amHost) {
            // Step 1
//                requestExitGame();
            // Step 2
            endPK();
            // Step 3
            exitGame();
        }
        GameEngine.destroy();

        // destroy RTM
        if (currentSceneRef != null) {
            if (amHost) currentSceneRef.delete(null);
            else currentSceneRef.unsubscribe(null);
            currentSceneRef = null;
        }
        if (targetSceneRef != null) {
            targetSceneRef.unsubscribe(null);
            targetSceneRef = null;
        }
    }
    //</editor-fold>

    //<editor-fold desc="SyncManager related">

    private void onJoinRTMSucceed(@NonNull SceneReference sceneReference) {
        BaseUtil.logD("onJoinRTMSucceed");
        currentSceneRef = sceneReference;
        _viewStatus.postValue(new ViewStatus.Message(localUser.getName() + " 加入RTM成功"));
        // 初始化时，监听当前频道属性
        if (currentSceneRef != null) {
            subscribeAttr(currentSceneRef, currentRoom);
            currentSceneRef.subscribe(new Sync.EventListener() {
                @Override
                public void onCreated(IObject item) {

                }

                @Override
                public void onUpdated(IObject item) {

                }

                @Override
                public void onDeleted(IObject item) {
                    _viewStatus.postValue(new ViewStatus.Error("主播已下播💔"));
                }

                @Override
                public void onSubscribeError(SyncManagerException ex) {

                }
            });
        }
    }

    public void subscribeAttr(@NonNull SceneReference sceneRef, @NonNull RoomInfo targetRoom) {
        if (Objects.equals(targetRoom.getId(), currentRoom.getId())) {
            BaseUtil.logD("subscribe current room attr");

            sceneRef.get(GameConstants.GIFT_INFO, (GetAttrCallback) this::tryHandleGetGiftInfo);
            sceneRef.get(GameConstants.PK_INFO, (GetAttrCallback) this::tryHandlePKInfo);
            sceneRef.subscribe(GameConstants.PK_INFO, new GamSyncEventListener(GameConstants.PK_INFO, this::tryHandlePKInfo));
            sceneRef.subscribe(GameConstants.GIFT_INFO, new GamSyncEventListener(GameConstants.GIFT_INFO, this::tryHandleGiftInfo));

            if (amHost) {
                sceneRef.get(GameConstants.PK_APPLY_INFO, (GetAttrCallback) this::tryHandleApplyPKInfo);
                sceneRef.subscribe(GameConstants.PK_APPLY_INFO, new GamSyncEventListener(GameConstants.PK_APPLY_INFO, this::tryHandleApplyPKInfo));

                sceneRef.get(GameConstants.GAME_APPLY_INFO, (GetAttrCallback) RoomViewModel.this::tryHandleGameApplyInfo);
                sceneRef.subscribe(GameConstants.GAME_APPLY_INFO, new GamSyncEventListener(GameConstants.GAME_APPLY_INFO, this::tryHandleGameApplyInfo));
            } else {
                sceneRef.get(GameConstants.PK_APPLY_INFO, (GetAttrCallback) this::justFetchValue);
                sceneRef.subscribe(GameConstants.PK_APPLY_INFO, new GamSyncEventListener(GameConstants.PK_APPLY_INFO, this::justFetchValue));

                sceneRef.get(GameConstants.GAME_INFO, (GetAttrCallback) this::tryHandleGameInfo);
                sceneRef.subscribe(GameConstants.GAME_INFO, new GamSyncEventListener(GameConstants.GAME_INFO, this::tryHandleGameInfo));
            }
        } else {
            BaseUtil.logD("subscribe other room attr");
            sceneRef.subscribe(GameConstants.PK_APPLY_INFO, new GamSyncEventListener(GameConstants.PK_APPLY_INFO, this::tryHandleApplyPKInfo));
            sceneRef.subscribe(GameConstants.GAME_APPLY_INFO, new GamSyncEventListener(GameConstants.GAME_APPLY_INFO, this::tryHandleGameApplyInfo));
        }
    }

    //<editor-fold desc="Gift related">

    private void tryHandleGiftInfo(IObject item) {
        BaseUtil.logD("tryHandleGiftInfo->" + System.currentTimeMillis());
        GiftInfo giftInfo = handleIObject(item, GiftInfo.class);
        if (giftInfo != null) {
            _gift.postValue(new Event<>(giftInfo));
        }
    }

    /**
     * 加入房间先获取当前 Gift
     */
    private void tryHandleGetGiftInfo(IObject item) {
        GiftInfo giftInfo = handleIObject(item, GiftInfo.class);
        if (giftInfo != null) {
            Event<GiftInfo> giftInfoEvent = new Event<>(giftInfo);
            giftInfoEvent.getContentIfNotHandled(); // consume this time
            _gift.postValue(giftInfoEvent);
        }
    }

    public void donateGift(@NonNull GiftInfo gift) {
        if (currentSceneRef != null && _gift.getValue() != null) {
            GiftInfo currentGift = _gift.getValue().peekContent();
            if (currentGift != null) {
                gift.setCoin(currentGift.getCoin() + gift.getCoin());
            }
            currentSceneRef.update(GameConstants.GIFT_INFO, gift, null);
        }
        // Currently in game mode, report it
        if (!amHost) {
            GameInfo gameInfo = _gameShareInfo.getValue();
            if (gameInfo != null && gameInfo.getStatus() == GameInfo.START) {
                SendGiftRequiredBean requiredBean = new SendGiftRequiredBean(currentRoom.getUserId(), gift.getGiftType() % 2 + 1, 1);
                GameEngine.getInstance().setOption(2, GameSetOptions.SEND_GIFT, GsonTool.objToJsonString(requiredBean));
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="PKApplyInfo related">
    private void tryHandleApplyPKInfo(IObject item) {
        PKApplyInfo pkApplyInfo = handleIObject(item, PKApplyInfo.class);
        if (pkApplyInfo != null) {
            if (_applyInfo.getValue() == null || !Objects.equals(_applyInfo.getValue().toString(), pkApplyInfo.toString()))
                onPKApplyInfoChanged(pkApplyInfo);
        }
    }

    private void justFetchValue(IObject item) {
        PKApplyInfo pkApplyInfo = handleIObject(item, PKApplyInfo.class);
        if (pkApplyInfo != null) {
            _applyInfo.postValue(pkApplyInfo);
        }
    }

    /**
     * 仅主播调用
     */
    private void onPKApplyInfoChanged(@NonNull PKApplyInfo pkApplyInfo) {
        BaseUtil.logD("onPKApplyInfoChanged:" + pkApplyInfo);
        _applyInfo.postValue(pkApplyInfo);
        switch (pkApplyInfo.getStatus()) {
            case PKApplyInfo.APPLYING: {
                // 当前不在PK && 收到其他主播的游戏邀请《==》加入对方RTM频道(同时监听对方频道的属性, 支持退出游戏后可以再次邀请进入游戏。)
                if (targetSceneRef == null && Objects.equals(pkApplyInfo.getTargetRoomId(), currentRoom.getId()))
                    Sync.Instance().joinScene(pkApplyInfo.getRoomId(), new Sync.JoinSceneCallback() {
                        @Override
                        public void onSuccess(SceneReference sceneReference) {
                            if (targetSceneRef == null)
                                targetSceneRef = sceneReference;
                            subscribeAttr(targetSceneRef, new RoomInfo(pkApplyInfo.getRoomId(), "", pkApplyInfo.getUserId()));
//                            targetSceneRef.subscribe(GameConstants.PK_APPLY_INFO, new GamSyncEventListener(GameConstants.PK_APPLY_INFO, RoomViewModel.this::tryHandleApplyPKInfo));
                        }

                        @Override
                        public void onFail(SyncManagerException exception) {

                        }
                    });
                break;
            }
            case PKApplyInfo.AGREED: {
                startApplyPK(pkApplyInfo);
                break;
            }
            case PKApplyInfo.REFUSED:
            case PKApplyInfo.END: {
                // ensure game end
                if (currentSceneRef != null)
                    currentSceneRef.update(GameConstants.GAME_INFO, new GameInfo(GameInfo.END, "", "", ""), null);

                endPK();
                break;
            }
        }
    }

    public void acceptApplyPK(@NonNull PKApplyInfo pkApplyInfo) {
        PKApplyInfo pkApplyInfo1 = pkApplyInfo.clone();
        pkApplyInfo1.setStatus(PKApplyInfo.AGREED);
        if (currentSceneRef != null)
            currentSceneRef.update(GameConstants.PK_APPLY_INFO, pkApplyInfo1, null);
    }

    public void cancelApplyPK(@NonNull PKApplyInfo pkApplyInfo) {
        PKApplyInfo desiredPK = pkApplyInfo.clone();
        desiredPK.setStatus(PKApplyInfo.REFUSED);

        boolean startedByMe = Objects.equals(localUser.getUserId(), desiredPK.getUserId());
        if (startedByMe) {
            if (targetSceneRef != null)
                targetSceneRef.update(GameConstants.PK_APPLY_INFO, desiredPK, null);
        } else {
            if (currentSceneRef != null)
                currentSceneRef.update(GameConstants.PK_APPLY_INFO, desiredPK, null);
        }
    }

    /**
     * 仅主播调用
     * 主播开始PK，为接收方接受发起方PK请求后的调用
     * Step 1. 根据当前角色生成 PKInfo
     * Step 2. 更新频道内{@link GameConstants#PK_INFO} 参数
     * Step 3. 更新频道内{@link GameConstants#GAME_APPLY_INFO} 参数
     */
    public void startApplyPK(@NonNull PKApplyInfo pkApplyInfo) {
        PKInfo pkInfo;
        GameApplyInfo gameApplyInfo = new GameApplyInfo(GameApplyInfo.PLAYING, pkApplyInfo.getGameId(), pkApplyInfo.getVendorId());
        if (Objects.equals(localUser.getUserId(), pkApplyInfo.getUserId())) {//      客户端为发起方
            BaseUtil.logD("发起方");
            pkInfo = new PKInfo(PKInfo.AGREED, pkApplyInfo.getTargetRoomId(), pkApplyInfo.getTargetUserId());
            if (targetSceneRef != null) {
                BaseUtil.logD("targetSceneRef");
                targetSceneRef.update(GameConstants.GAME_APPLY_INFO, gameApplyInfo, (GetAttrCallback) result -> BaseUtil.logD("onSuccess(:" + result.toString()));
            }
        } else {//      客户端为接收方,当前房间内所有人需要知道发起方的 roomId 和 UserId
            BaseUtil.logD("接收方");
            pkInfo = new PKInfo(PKInfo.AGREED, pkApplyInfo.getRoomId(), pkApplyInfo.getUserId());
        }

        if (currentSceneRef != null) {
            currentSceneRef.update(GameConstants.PK_INFO, pkInfo, null);
        }
    }

    /**
     * 向其他主播(不同的频道)发送PK邀请
     * <p>
     * **RTM 限制订阅只能在加入频道的情况下发生**
     * <p>
     * 1. 加入对方频道
     * 2. 监听对方频道属性
     * 3. 往对方频道添加属性 pkApplyInfo
     *
     * @param roomViewModel We want to separate the logic with different UI, but a RoomViewModel is still needed.
     * @param targetRoom    对方的 RoomInfo
     * @param gameId        Currently only have one game. Ignore this.
     */
    public void sendApplyPKInvite(@NonNull RoomViewModel roomViewModel, @NonNull RoomInfo targetRoom, @NonNull String gameId, @NonNull String vendorid) {
        if (targetSceneRef != null)
            doSendApplyPKInvite(roomViewModel, targetSceneRef, targetRoom, gameId, vendorid);
        else
            Sync.Instance().joinScene(targetRoom.getId(), new Sync.JoinSceneCallback() {
                @Override
                public void onSuccess(SceneReference sceneReference) {
                    targetSceneRef = sceneReference;
                    // 发起邀请，监听对方频道
                    roomViewModel.subscribeAttr(sceneReference, targetRoom);
                    doSendApplyPKInvite(roomViewModel, targetSceneRef, targetRoom, gameId, vendorid);
                }

                @Override
                public void onFail(SyncManagerException exception) {
                    _pkResult.postValue(new Event<>(false));
                }
            });
    }

    private void doSendApplyPKInvite(@NonNull RoomViewModel roomViewModel, @NonNull SceneReference sceneReference, RoomInfo targetRoom, @NonNull String gameId, @NonNull String vendorid) {
        PKApplyInfo pkApplyInfo = new PKApplyInfo(roomViewModel.currentRoom.getUserId(), targetRoom.getUserId(), localUser.getName(), PKApplyInfo.APPLYING, gameId,
                roomViewModel.currentRoom.getId(), targetRoom.getId(), vendorid);

        sceneReference.update(GameConstants.PK_APPLY_INFO, pkApplyInfo, new Sync.DataItemCallback() {
            @Override
            public void onSuccess(IObject result) {
                BaseUtil.logD("success update:" + result.getId() + "->" + result);
                _pkResult.postValue(new Event<>(true));
            }

            @Override
            public void onFail(SyncManagerException exception) {
                _pkResult.postValue(new Event<>(false));
            }
        });
    }

    //</editor-fold>

    //<editor-fold desc="GameInfo">
    private void tryHandleGameInfo(IObject item) {
        GameInfo gameInfo = handleIObject(item, GameInfo.class);
        if (gameInfo != null) {
            if (_gameShareInfo.getValue() == null || !Objects.equals(_gameShareInfo.getValue().toString(), gameInfo.toString()))
                onGameInfoChanged(gameInfo);
        }
    }

    /**
     * 只在当前频道
     * 观众：{@link GameInfo#START} 订阅视频流 ,{@link GameInfo#END} 取消订阅
     */
    private void onGameInfoChanged(@NonNull GameInfo gameInfo) {
        BaseUtil.logD("onGameShareInfoChanged");
        if (gameInfo.getStatus() == GameInfo.START)
            roomGame = new AgoraGame(gameInfo.getGameId(), "", "", gameInfo.getVendorId());
        else
            roomGame = null;
        _gameShareInfo.postValue(gameInfo);
    }
    //</editor-fold>

    //<editor-fold desc="GameApplyInfo">
    private void tryHandleGameApplyInfo(IObject item) {
        BaseUtil.logD("tryHandleGameApplyInfo->" + item.toString());
        GameApplyInfo currentGame = handleIObject(item, GameApplyInfo.class);
        if (currentGame != null) {
            onGameApplyInfoChanged(currentGame);
        }
    }

    private void onGameApplyInfoChanged(@NonNull GameApplyInfo currentGame) {
        BaseUtil.logD("onGameApplyInfoChanged:" + currentGame);
        if (currentGame.getStatus() == GameApplyInfo.PLAYING) {
            roomGame = new AgoraGame(currentGame.getGameId(), "", "", currentGame.getVendorId());
            PKApplyInfo applyInfo = _applyInfo.getValue();
            if (currentSceneRef != null && applyInfo != null) {
                String targetRoomId = applyInfo.getRoomId().equals(currentRoom.getId()) ? applyInfo.getTargetRoomId() : currentRoom.getId();
                currentSceneRef.update(GameConstants.GAME_INFO, new GameInfo(GameInfo.START, targetRoomId, currentGame.getGameId(), currentGame.getVendorId()), null);
            }
        } else if (currentGame.getStatus() == GameApplyInfo.END) {
            if (currentSceneRef != null)
                currentSceneRef.update(GameConstants.GAME_INFO, new GameInfo(GameInfo.END, "", currentGame.getGameId(), currentGame.getVendorId()), null);
        }
        _currentGame.postValue(currentGame);
    }

    /**
     * 只更新监听的{@link GameConstants#GAME_APPLY_INFO} 字段
     */
    public void requestExitGame() {
        GameApplyInfo gameApplyInfo = _currentGame.getValue();
        if (gameApplyInfo == null) return;

        GameApplyInfo desiredGameApplyInfo = new GameApplyInfo(GameApplyInfo.END, gameApplyInfo.getGameId(), gameApplyInfo.getVendorId());

        PKApplyInfo pkApplyInfo = _applyInfo.getValue();

        if (pkApplyInfo != null) {
            boolean startedByMe = Objects.equals(localUser.getUserId(), pkApplyInfo.getUserId());
            if (!startedByMe) {
                if (currentSceneRef != null)
                    currentSceneRef.update(GameConstants.GAME_APPLY_INFO, desiredGameApplyInfo, null);
            } else {
                if (targetSceneRef != null)
                    targetSceneRef.update(GameConstants.GAME_APPLY_INFO, desiredGameApplyInfo, null);
            }
        }
    }
    //</editor-fold>

    //<editor-fold desc="PKInfo">
    private void tryHandlePKInfo(IObject item) {
        PKInfo pkInfo = handleIObject(item, PKInfo.class);
        if (pkInfo != null)
            onPKInfoChanged(pkInfo);
    }

    /**
     * {@link PKInfo#AGREED} 加入对方频道，拉流 | {@link PKInfo#END} 退出频道
     */
    private void onPKInfoChanged(@NonNull PKInfo pkInfo) {
        BaseUtil.logD("onPKInfoChanged:" + pkInfo);
        if (pkInfo.getStatus() == PKInfo.AGREED) {
            // 只用来加入频道，只使用 roomId 字段
            // this variable will only for join channel so room name doesn't matter.
            RoomInfo subRoom = new RoomInfo(pkInfo.getRoomId(), "", pkInfo.getUserId());
            joinSubRoom(subRoom);
        } else if (pkInfo.getStatus() == PKInfo.END) {
            leaveSubRoom();
        }
    }

    /**
     * 仅主播调用
     */
    public void endPK() {
        PKInfo pkInfo = new PKInfo(PKInfo.END, "", "");
        PKApplyInfo pkApplyInfo = applyInfo().getValue();
        boolean startedByMe = false;
        if (pkApplyInfo != null) {
            pkApplyInfo = pkApplyInfo.clone();
            pkApplyInfo.setStatus(PKApplyInfo.END);
            startedByMe = Objects.equals(localUser.getUserId(), pkApplyInfo.getUserId());
        }
        if (currentSceneRef != null) {
            currentSceneRef.update(GameConstants.PK_INFO, pkInfo, null);
            if (pkApplyInfo != null && !startedByMe) {
                currentSceneRef.update(GameConstants.PK_APPLY_INFO, pkApplyInfo, null);
            }
        }
        if (targetSceneRef != null) {
            if (pkApplyInfo != null && startedByMe)
                targetSceneRef.update(GameConstants.PK_APPLY_INFO, pkApplyInfo, null);
            BaseUtil.logD("targetSceneRef unsubscribe");
            targetSceneRef.unsubscribe(null);
            targetSceneRef = null;
        }
    }

    //</editor-fold>


    @Nullable
    private <T> T handleIObject(IObject obj, Class<T> clazz) {
        T res = null;
        try {
            res = obj.toObject(clazz);
        } catch (Exception e) {
            e.printStackTrace();
            BaseUtil.logD(e.getMessage());
        }
        return res;
    }

    //</editor-fold>

    //<editor-fold desc="RTC related">
    public void muteLocalVideoStream(boolean mute) {
        isLocalVideoMuted = mute;
        rtcEngineEx.muteLocalVideoStream(isLocalVideoMuted);
    }

    public void muteLocalAudioStream(boolean mute) {
        isLocalMicMuted = mute;
        rtcEngineEx.muteLocalAudioStream(isLocalMicMuted);
    }

    public void flipCamera() {
        rtcEngineEx.switchCamera();
    }

    /**
     * 加入当前房间
     */
    public void joinRoom() {
        ChannelMediaOptions options = new ChannelMediaOptions();
        options.autoSubscribeAudio = true;
        options.autoSubscribeVideo = true;
        options.publishCameraTrack = amHost;
        options.publishAudioTrack = amHost;
        options.clientRoleType = amHost ? Constants.CLIENT_ROLE_BROADCASTER : Constants.CLIENT_ROLE_AUDIENCE;
        rtcEngineEx.joinChannel("", currentRoom.getId(), Integer.parseInt(localUser.getUserId()), options);
    }

    /**
     * 加入其他主播房间前先退出当前已加入的其他主播房间
     * 加入成功监听到对方主播上线《==》UI更新
     */
    public void joinSubRoom(@NonNull RoomInfo subRoomInfo) {

        tempSubRoom = subRoomInfo;

        RoomInfo tempRoom = _subRoomInfo.getValue();
        if (tempRoom != null && !tempRoom.getId().equals(subRoomInfo.getId())) {
            leaveSubRoom();
        }

        RtcConnection connection = new RtcConnection();
        connection.channelId = subRoomInfo.getId();
        connection.localUid = -Integer.parseInt(localUser.getUserId());

        ChannelMediaOptions options = new ChannelMediaOptions();
        rtcEngineEx.joinChannelEx("", connection, options, mySubEventHandler);
    }

    public void leaveSubRoom() {
        RoomInfo tempRoom = _subRoomInfo.getValue();
        if (tempRoom == null) return;
        String roomId = tempRoom.getId();
        _subRoomInfo.postValue(null);
        RtcConnection connection = new RtcConnection();
        connection.channelId = roomId;
        connection.localUid = -Integer.parseInt(localUser.getUserId());
        rtcEngineEx.leaveChannelEx(connection);
    }

    private void configRTC(@NonNull RtcEngineEx engine) {
        if (amHost) {
            engine.enableAudio();
            engine.enableVideo();
            engine.startPreview();
        }
    }

    private void configRTM() {
        // 监听当前房间数据 <==> 礼物、PK、
        Sync.Instance().joinScene(currentRoom.getId(), new Sync.JoinSceneCallback() {
            @Override
            public void onSuccess(SceneReference sceneReference) {
                onJoinRTMSucceed(sceneReference);
            }

            @Override
            public void onFail(SyncManagerException e) {
                _viewStatus.postValue(new ViewStatus.Error("主播已下播💔"));
            }
        });
    }

    private void configGameEngine() {
        Context context = ((RtcEngineImpl) rtcEngineEx).getContext();
        LogConfig logConfig = new LogConfig(context.getExternalCacheDir().getAbsolutePath(), Level.CONFIG);
        GameEngine.init(new GameContext(context, context.getString(R.string.game_app_id)
                , localUser.getUserId(), GlobalViewModel.gameToken, logConfig
                , new IGameEngineEventHandler() {
            @Override
            public void onSetOptionResult(int operationId, String option, boolean result, String reason) {
                BaseUtil.logD("onSetOptionResult：operationId=" + operationId + "，option=" + option + "，result=" + result + "，reason=" + result);
            }

            @Override
            public void onGetOptionResult(int operationId, String option, boolean result, String outOptions) {
                BaseUtil.logD("onGetOptionResult：operationId=" + operationId + "，option=" + option + "，result=" + result + "，reason=" + result + "，outOptions=" + outOptions);
                if (option.equals(GameGetOptions.GET_GAME_LIST)) {
                    if (result) {
                        AgoraGameList list = GsonTool.toObject(outOptions, new TypeToken<AgoraGameList>() {
                        }.getType());
                        if (null != list) {
                            totalPage = list.getTotalPage();
                            _gameList.setValue(list.getItems());
                        }
                    } else {
                        BaseUtil.logD(outOptions);
                    }
                }
            }

            @Override
            public void onMessage(String messageId, String message) {
                if (GameUtil.sudGameLoad(messageId, message)) {
                    _viewStatus.postValue(new ViewStatus.Done());
                } else if (GameUtil.sudGameExpired(messageId, message)) {
                    _viewStatus.postValue(new ViewStatus.Update());
                } else {
                    _viewStatus.postValue(new ViewStatus.Message(messageId + " -> " + message));
                }
            }
        }, GameUtil.huranEnv == GameEnvType.ENV_HURAN_TEST));

        GameEngine.getInstance().setOption(2, "set_game_center_test_env", String.valueOf(GameUtil.gameEnv == GameEnvType.ENV_AGORA_TEST));
    }

    public void setupLocalPreview(@NonNull TextureView view) {
        rtcEngineEx.setupLocalVideo(new VideoCanvas(view, Constants.RENDER_MODE_HIDDEN, Integer.parseInt(localUser.getUserId())));
    }

    public void stopLocalPreview() {
        rtcEngineEx.stopPreview();
    }

    /**
     * @param view        用来构造 videoCanvas
     * @param roomInfo    isLocalHost => current RoomInfo，!isLocalHost => 对方的 RoomInfo
     * @param isLocalHost 是否是当前房间主播
     */
    public void setupRemotePreview(@NonNull TextureView view, @NonNull RoomInfo roomInfo, boolean isLocalHost) {
        VideoCanvas videoCanvas = new VideoCanvas(view, Constants.RENDER_MODE_HIDDEN, Integer.parseInt(roomInfo.getUserId()));
        if (isLocalHost) {
            rtcEngineEx.setupRemoteVideo(videoCanvas);
        } else {
            RtcConnection connection = new RtcConnection();
            connection.channelId = roomInfo.getId();
            connection.localUid = -Integer.parseInt(localUser.getUserId());
            rtcEngineEx.setupRemoteVideoEx(videoCanvas, connection);
        }
    }

    //</editor-fold>

    //<editor-fold desc="Game">
    public void startGame(@Nullable FrameLayout gameContainerFgRoom, @Nullable WebView webView, @NonNull Rect safePadding) {
        if (roomGame == null) return;
        String roomId = null;
        if (amHost) {
            PKApplyInfo pkApplyInfo = _applyInfo.getValue();
            if (pkApplyInfo != null)
                roomId = pkApplyInfo.getRoomId().equals(currentRoom.getId()) ? pkApplyInfo.getTargetRoomId() : currentRoom.getId();
        } else {
            GameInfo gameShareInfo = _gameShareInfo.getValue();
            if (gameShareInfo != null)
                roomId = gameShareInfo.getRoomId();
        }
        if (roomId != null) {
            String language = Locale.getDefault().getLanguage().equalsIgnoreCase("zh") ? "zh-CN" : "en";
            JoinGameRequiredBean joinGameRequiredBean = new JoinGameRequiredBean(safePadding, localUser.getName(), localUser.getAvatar(), amHost ? null : currentRoom.getUserId());

            GameOptions gameOptions = new GameOptions(roomGame.getGameId(), roomId, localUser.getUserId(), getIdentification(roomId), language, GsonTool.objToJsonString(joinGameRequiredBean));

            if (webView == null && gameContainerFgRoom != null)
                doStartGame(gameOptions, gameContainerFgRoom);
            else if (webView != null && gameContainerFgRoom == null)
                doStartGame(gameOptions, webView);
            else {
                _viewStatus.postValue(new ViewStatus.Message("Wrong invoke, container and WebView both null."));
            }
        }
    }

    //</editor-fold>

    //<editor-fold desc="Sud Game">
    public void startSudGame(@NonNull FrameLayout gameContainerFgRoom, @NonNull String gameCode, @NonNull String sudAppId, @NonNull String sudAppKey, @NonNull Rect safePadding) {
        if (roomGame == null) return;
        String roomId = null;
        if (amHost) {
            PKApplyInfo pkApplyInfo = _applyInfo.getValue();
            if (pkApplyInfo != null)
                roomId = pkApplyInfo.getRoomId().equals(currentRoom.getId()) ? pkApplyInfo.getTargetRoomId() : currentRoom.getId();
        } else {
            GameInfo gameShareInfo = _gameShareInfo.getValue();
            if (gameShareInfo != null)
                roomId = gameShareInfo.getRoomId();
        }
        if (roomId != null) {
            String language = Locale.getDefault().getLanguage().equalsIgnoreCase("zh") ? "zh-CN" : "en";
            SudAdditionalRequired additionalRequired = new SudAdditionalRequired(gameCode, sudAppId, sudAppKey, gameContainerFgRoom.getWidth(), gameContainerFgRoom.getHeight());
            SudJoinGameRequiredBean joinGameRequiredBean = new SudJoinGameRequiredBean(safePadding, localUser.getName(), localUser.getAvatar(), amHost ? null : currentRoom.getUserId(), additionalRequired);

            GameOptions gameOptions = new GameOptions(roomGame.getGameId(), roomId, localUser.getUserId(), getIdentification(roomId), language, roomGame.getVendorId(), GsonTool.objToJsonString(joinGameRequiredBean));

            Activity activity = GameUtil.getActivityFromView(gameContainerFgRoom);
            if (activity != null)
                doStartGame(gameOptions, activity);
            else {
                _viewStatus.postValue(new ViewStatus.Message("Wrong invoke, activity is null."));
            }
        }
    }

    private void doStartGame(@NonNull GameOptions options, @NonNull WebView webView) {
        GameEngine.getInstance().loadGame(options, webView);
    }

    private void doStartGame(@NonNull GameOptions options, @NonNull ViewGroup viewGroup) {
        GameEngine.getInstance().loadGame(options, viewGroup);
    }

    private void doStartGame(@NonNull GameOptions options, @NonNull Activity activity) {
        GameEngine.getInstance().loadGame(options, activity);
    }

    /**
     * 根据 userId 获取当前玩家在游戏中的角色
     */
    private String getIdentification(String gameRoomId) {
        if (amHost) {
            if (Objects.equals(currentRoom.getId(), gameRoomId))
                return "2";  // 副玩家
            else return "1"; // 主玩家-房主
        } else {
            return "3";     // 观众
        }
    }

    /**
     * 监听到修改成功，退出游戏
     */
    public void exitGame() {
        GameEngine.getInstance().leaveGame();
    }

    public void fetchGameList() {
        currentPage = 1;
        _gameList.setValue(new ArrayList<>());
        BaseUtil.logD("fetchGameList page " + currentPage);
        FetchGameListRequiredBean bean = new FetchGameListRequiredBean(GAME_LIMIT, currentPage);
        GameEngine.getInstance().getOption(0, GameGetOptions.GET_GAME_LIST, GsonTool.objToJsonString(bean));
    }

    public void fetchGameListMore() {
        currentPage++;
        if (totalPage >= currentPage) {
            BaseUtil.logD("fetchGameList more page " + currentPage);
            FetchGameListRequiredBean bean = new FetchGameListRequiredBean(GAME_LIMIT, currentPage);
            GameEngine.getInstance().getOption(0, GameGetOptions.GET_GAME_LIST, GsonTool.objToJsonString(bean));
        }
    }

    //</editor-fold>

    private interface GetAttrCallback extends Sync.DataItemCallback {
        @Override
        default void onFail(SyncManagerException exception) {

        }
    }

    public View getGameView() {
        return GameEngine.getInstance().getGameView();
    }

    public void updateGameCode(String appId) {
        getGameCode(appId, new GameGetCodeListener() {
            @Override
            public void onSuccess(String code) {
                Map<String, Object> jsonData = new HashMap<>(4);
                jsonData.put("game_code", code);
                SudGameMessage<Map<String, Object>> message = new SudGameMessage<>(GameConstants.COMMON_SELF_UPDATE_CODE, jsonData);
                // operationId 任意值
                GameEngine.getInstance().setOption(2, GameSetOptions.GAME_STATE, GsonTool.objToJsonString(message));
            }

            @Override
            public void onFailed() {
            }
        });

    }

    public void getGameCode(String appId, GameGetCodeListener listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", localUser.getUserId());
        params.put("app_id", appId);
        GameHttpServer.get().enqueuePost("https://txy.jyanedu.com/sud/login", new HashMap<>(), params,
                SudLoginBean.class, new GameHttpServer.IHttpCallback<SudLoginBean>() {
                    @Override
                    public void onSuccess(String bodyString, SudLoginBean data) {
                        if (data.getRet_code() == 0 && null != data.getData()) {
                            listener.onSuccess(data.getData().getCode());
                        } else {
                            listener.onFailed();
                        }
                    }

                    @Override
                    public void onFail(int code, String message) {
                        listener.onFailed();
                    }
                });
    }

    /**
     * 游戏login(getCode)监听
     */
    public interface GameGetCodeListener {
        /**
         * 成功
         */
        void onSuccess(String code);

        /**
         * 失败
         */
        void onFailed();
    }
}
