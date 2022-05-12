package io.agora.scene.rtegame.bean;

import androidx.annotation.NonNull;

public class AgoraGame {
    @NonNull
    private final String gameId;
    @NonNull
    private final String gameName;
    @NonNull
    private final String iconUrl;

    public AgoraGame(@NonNull String gameId, @NonNull String gameName, @NonNull String iconUrl) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getGameId() {
        return gameId;
    }

    @NonNull
    public String getGameName() {
        return gameName;
    }

    @NonNull
    public String getIconUrl() {
        return iconUrl;
    }
}

