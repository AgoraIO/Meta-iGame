package io.agora.scene.rtegame.bean.sdk;

import java.util.List;

import io.agora.scene.rtegame.bean.AgoraGame;

public class AgoraGameList {
    private final int totalItem;
    private final int totalPage;
    private final int currentPage;
    private final int size;
    private final List<AgoraGame> items;

    public AgoraGameList(int totalItem, int totalPage, int currentPage, int size, List<AgoraGame> items) {
        this.totalItem = totalItem;
        this.totalPage = totalPage;
        this.currentPage = currentPage;
        this.size = size;
        this.items = items;
    }

    public int getTotalItem() {
        return totalItem;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getSize() {
        return size;
    }

    public List<AgoraGame> getItems() {
        return items;
    }
}
