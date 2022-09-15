package io.agora.scene.rtegame.bean.api;

public class SudBaseResponse<T> {
    private int ret_code;
    private int ret_msg;
    private T data;

    public int getRet_code() {
        return ret_code;
    }

    public void setRet_code(int ret_code) {
        this.ret_code = ret_code;
    }

    public int getRet_msg() {
        return ret_msg;
    }

    public void setRet_msg(int ret_msg) {
        this.ret_msg = ret_msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
