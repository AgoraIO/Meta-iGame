package io.agora.scene.rtegame.bean.sdk;

import java.util.Locale;

public class FetchGameListRequiredBean {
    private final int limit;
    private final int page;
    private final String language;

    public FetchGameListRequiredBean(int limit, int page) {
        this.limit = limit;
        this.page = page;
        this.language = Locale.getDefault().getLanguage().equalsIgnoreCase("zh") ? "zh-CN" : "en";
    }

    public int getLimit() {
        return limit;
    }

    public int getPage() {
        return page;
    }

    public String getLanguage() {
        return language;
    }
}
