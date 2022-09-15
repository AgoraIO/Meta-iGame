package io.agora.scene.rtegame.bean.api;

public class SudLoginBean extends SudBaseResponse<SudLoginBean>{

    private String code;
    private long expire_date;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExpire_date() {
        return expire_date;
    }

    public void setExpire_date(long expire_date) {
        this.expire_date = expire_date;
    }
}
