package io.agora.scene.rtegame.bean.sdk;

public class SendGiftPayLoad {
    private final int giftCost;
    private final int count;

    public SendGiftPayLoad(int giftCost, int count) {
        this.giftCost = giftCost;
        this.count = count;
    }

    public int getGiftCost() {
        return giftCost;
    }

    public int getCount() {
        return count;
    }
}
