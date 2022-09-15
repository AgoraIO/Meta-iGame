package io.agora.scene.rtegame.bean.sdk;

public class SudAdditionalRequired {

    private final String gameCode;
    private final String gameAppId;
    private final String gameAppKey;
    private final int width;
    private final int height;

    public SudAdditionalRequired(String gameCode, String gameAppId, String gameAppKey, int width, int height) {
        this.gameCode = gameCode;
        this.gameAppId = gameAppId;
        this.gameAppKey = gameAppKey;
        this.width = width;
        this.height = height;
    }

    public String getGameCode() {
        return gameCode;
    }

    public String getGameAppId() {
        return gameAppId;
    }

    public String getGameAppKey() {
        return gameAppKey;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
