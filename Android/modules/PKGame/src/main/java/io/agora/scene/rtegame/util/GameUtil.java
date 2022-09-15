package io.agora.scene.rtegame.util;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;

import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import io.agora.example.base.BaseUtil;
import io.agora.gamesdk.annotations.GameSetOptions;
import io.agora.scene.rtegame.GlobalViewModel;
import io.agora.scene.rtegame.GlobalViewModelFactory;
import io.agora.scene.rtegame.R;
import io.agora.scene.rtegame.bean.RoomInfo;
import io.agora.scene.rtegame.bean.sdk.SudGameMessage;
import io.agora.syncmanager.rtm.Scene;

public class GameUtil {

    // 声网环境默认正式
    @GameEnvType
    public static int gameEnv = GameEnvType.ENV_AGORA_OFFICIAL;
    // 忽然环境默认测试
    @GameEnvType
    public static int huranEnv = GameEnvType.ENV_HURAN_OFFICIAL;

    public static boolean showGiftEffect = true;
    public static boolean usingSDKWebView = true;
    //    0-无头像昵称(默认），1-有昵称无头像，2-有昵称有头像，3-无眤称有头像
    public static int avatarType = 2;

    public static boolean showAvatar = true;
    public static boolean showNickname = true;

    private static final String[] avatarList = {
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/012scw_ons_crd_02.jpg",
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/003cap_ons_crd_03.jpg",
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/011blw_ons_crd_04.jpg",
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/009drs_ons_crd_02.jpg",
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/017lok_ons_crd_03.jpg",
            "https://terrigen-cdn-dev.marvel.com/content/prod/1x/004tho_ons_crd_03.jpg"};

    @NonNull
    public static String randomAvatar() {
        int index = new Random().nextInt(10000);
        return avatarList[index % avatarList.length];
    }


    private static final String[] nameList = {
            "Tokyo",
            "Delhi",
            "Shanghai",
            "Sao Paulo",
            "Mexico City",
            "Cairo",
            "Dhaka",
            "Mumbai",
            "Beijing",
            "Osaka",
            "Aruba",
            "Jamaica",
            "Bermuda",
            "Bahama",
            "Key Largo",
            "Montego",
            "Kokomo",
    };

    @DrawableRes
    public static int getBgdByRoomBgdId(@Nullable String bgdId) {
        int i = 1;
        try {
            if (bgdId != null)
                i = Integer.parseInt(bgdId.toLowerCase().substring(8, 10));
        } catch (Exception ignored) {
        }
        switch (i) {
            case 1:
                return R.drawable.game_portrait01;
            case 2:
                return R.drawable.game_portrait02;
            case 3:
                return R.drawable.game_portrait03;
            case 4:
                return R.drawable.game_portrait04;
            case 5:
                return R.drawable.game_portrait05;
            case 6:
                return R.drawable.game_portrait06;
            case 7:
                return R.drawable.game_portrait07;
            case 8:
                return R.drawable.game_portrait08;
            case 9:
                return R.drawable.game_portrait09;
            case 10:
                return R.drawable.game_portrait10;
            case 11:
                return R.drawable.game_portrait11;
            case 12:
                return R.drawable.game_portrait12;
            case 13:
                return R.drawable.game_portrait13;
            default:
                return R.drawable.game_portrait14;
        }
    }

    @DrawableRes
    public static int getGameBgdByGameId(int gameId) {
        return R.drawable.game_pic_bgd_game_1;
    }

    @NonNull
    public static Scene getSceneFromRoomInfo(@NonNull RoomInfo roomInfo) {
        Scene scene = new Scene();
        scene.setId(roomInfo.getId());
        scene.setUserId(roomInfo.getUserId());

        HashMap<String, String> map = new HashMap<>();
        map.put("backgroundId", roomInfo.getBackgroundId());
        map.put("roomName", roomInfo.getRoomName());
        map.put("roomId", roomInfo.getId());

        scene.setProperty(map);
        return scene;
    }

    @NonNull
    public static String getRandomRoomName() {
        return getRandomRoomName(new Random().nextInt(10000));
    }

    @NonNull
    public static String getRandomRoomName(int number) {
        return nameList[number % nameList.length];
    }


    public static void setBottomMarginForConstraintLayoutChild(@NonNull View view, int desiredBottom) {
        ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        if (lp != null) {
            lp.bottomMargin = desiredBottom;
            view.setLayoutParams(lp);
        }
    }

    public static void setBottomDialogBackground(@NonNull View view) {
        MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();

        ShapeAppearanceModel shapeAppearanceModel = ShapeAppearanceModel.builder(view.getContext(),
                R.style.game_cornerNormalTopStyle, R.style.game_cornerNormalTopStyle).build();
        materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);

        int desiredColor = BaseUtil.getColorInt(view.getContext(), R.attr.colorSurface);
        desiredColor = getMaterialBackgroundColor(desiredColor);
        materialShapeDrawable.setFillColor(ColorStateList.valueOf(desiredColor));
        ViewCompat.setBackground(view, materialShapeDrawable);
    }

    public static boolean isNightMode() {
        return isNightMode(Resources.getSystem().getConfiguration());
    }

    public static boolean isNightMode(@NonNull Configuration configuration) {
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getMaterialBackgroundColor(int color) {
        if (color == Color.BLACK)
            return Color.parseColor("#2D2D2D");
        else return color;
    }

    public static <T extends ViewModel> T getViewModel(@NonNull Class<T> viewModelClass, @NonNull ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner, new ViewModelProvider.NewInstanceFactory()).get(viewModelClass);
    }

    public static <T extends ViewModel> T getViewModel(@NonNull Class<T> viewModelClass, @NonNull ViewModelProvider.NewInstanceFactory factory, @NonNull ViewModelStoreOwner owner) {
        return new ViewModelProvider(owner, factory).get(viewModelClass);
    }

    @NonNull
    public static GlobalViewModel getAndroidViewModel(@NonNull ComponentActivity activity) {
        return new ViewModelProvider(activity, new GlobalViewModelFactory(activity.getApplication())).get(GlobalViewModel.class);
    }

    @NonNull
    public static GlobalViewModel getAndroidViewModel(@NonNull Fragment fragment) {
        return new ViewModelProvider(fragment.requireActivity(), new GlobalViewModelFactory(fragment.requireActivity().getApplication())).get(GlobalViewModel.class);
    }

    public static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    public static boolean isSud(String vendorId) {
        return "10021".equals(vendorId);
    }

    public static Activity getActivityFromView(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        return null;
    }

    public static SudGameMessage<Map<String, Object>> sudGameState(String messageId, String message) {
        SudGameMessage<Map<String, Object>> gameMessage = null;
        if (GameSetOptions.GAME_STATE.equals(messageId)) {
            gameMessage = GsonTool.toObject(message, new TypeToken<SudGameMessage<Map<String, Object>>>() {
            }.getType());
        }
        return gameMessage;
    }

    public static boolean sudGameLoad(String messageId, String message) {
        boolean isGameLoadSuccess = false;
        SudGameMessage<Map<String, Object>> gameMessage = sudGameState(messageId, message);
        if (null != gameMessage) {
            if (GameConstants.GAME_COMMON_LOAD.equals(gameMessage.getState())) {
                Map<String, Object> stateData = gameMessage.getData();
                Object value = stateData.get("code");
                int code = parseInt(value, -1);
                isGameLoadSuccess = code == 0;
            }
        }
        return isGameLoadSuccess;
    }

    public static boolean sudGameExpired(String messageId, String message) {
        boolean isGameCodeExpired = false;
        SudGameMessage<Map<String, Object>> gameMessage = sudGameState(messageId, message);
        if (null != gameMessage) {
            if (GameConstants.GAME_COMMON_EXPIRED.equals(gameMessage.getState())) {
                isGameCodeExpired = true;
            }
        }
        return isGameCodeExpired;
    }

    private static int parseInt(Object value, int defaultValue) {
        if (value == null) return defaultValue;
        try {
            return Integer.parseInt(value.toString());
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
