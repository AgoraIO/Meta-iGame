package io.agora.scene.rtegame.bean.sdk;

public class SendGiftRequiredBean {
    private final String to;
    private final SendGiftPayLoad payload;

    public SendGiftRequiredBean(String to, int giftCost, int count) {
        this.to = to;
        this.payload = new SendGiftPayLoad(giftCost, count);
    }

    public String getTo() {
        return to;
    }

    public SendGiftPayLoad getPayload() {
        return payload;
    }

}
