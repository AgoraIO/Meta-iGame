package io.agora.scene.rtegame.bean.sdk;

import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class JoinGameRequiredBean {
    private final int left;
    private final int top;
    private final int right;
    private final int bottom;
    @NonNull
    private final String name;
    @NonNull
    private final String avatar;
    @Nullable
    private final String to_user;
    private final int avatar_type = 2;
    private final int time_limit = 360;
    private final int show_join;
    private final int show_ready;
    private final int show_start;
    private final int show_kickout;

    public JoinGameRequiredBean(@NonNull Rect safePadding, @NonNull String name, @NonNull String avatar, @Nullable String targetUser) {
        this.left = safePadding.left;
        this.top = safePadding.top;
        this.right = safePadding.right;
        this.bottom = safePadding.bottom;
        this.name = name;
        this.avatar = avatar;
        this.to_user = targetUser;
        this.show_join = 1;
        this.show_ready = 1;
        this.show_start = 1;
        this.show_kickout = 1;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getRight() {
        return right;
    }

    public int getBottom() {
        return bottom;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getAvatar() {
        return avatar;
    }

    @Nullable
    public String getTo_user() {
        return to_user;
    }

    public int getShow_join() {
        return show_join;
    }

    public int getShow_ready() {
        return show_ready;
    }

    public int getShow_start() {
        return show_start;
    }

    public int getShow_kickout() {
        return show_kickout;
    }

    public int getAvatar_type() {
        return avatar_type;
    }
}
