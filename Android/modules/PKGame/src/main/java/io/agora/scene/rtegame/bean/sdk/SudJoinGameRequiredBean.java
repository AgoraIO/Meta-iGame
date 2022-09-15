package io.agora.scene.rtegame.bean.sdk;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SudJoinGameRequiredBean extends JoinGameRequiredBean {

    private final String game_code;
    private final String game_appid;
    private final String game_appkey;
    private final int width;
    private final int height;

    public SudJoinGameRequiredBean(@NonNull Rect safePadding, @NonNull String name, @NonNull String avatar, @Nullable String targetUser, @NonNull SudAdditionalRequired additionalRequired) {
        super(safePadding, name, avatar, targetUser);
        this.game_code = additionalRequired.getGameCode();
        this.game_appid = additionalRequired.getGameAppId();
        this.game_appkey = additionalRequired.getGameAppKey();
        this.width = additionalRequired.getWidth();
        this.height = additionalRequired.getHeight();
    }
}
