package io.agora.scene.rtegame.api;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import io.agora.example.base.BaseUtil;
import io.agora.scene.rtegame.util.GsonTool;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GameHttpServer {

    private GameHttpServer() {
    }

    public static GameHttpServer get() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final GameHttpServer INSTANCE = new GameHttpServer();
    }

    private OkHttpClient okHttpClient;

    private final Handler handler = new Handler(Looper.getMainLooper());

    private OkHttpClient client() {
        if (null != okHttpClient) {
            return okHttpClient;
        }
        okHttpClient = new OkHttpClient.Builder().build();
        return okHttpClient;
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    public <T> void enqueueGet(String url, Map<String, Object> headers, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
        Request.Builder builder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        Request request = builder.url(url).get().build();
        enqueue(client().newCall(request), responseClazz, httpCallback);
    }

    public <T> void enqueuePost(String url, Map<String, Object> headers, Map<String, Object> params, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
        RequestBody body = RequestBody.create(GsonTool.objToJsonString(params), MediaType.get("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder();
        if (headers != null && !headers.isEmpty()) {
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(), entry.getValue().toString());
            }
        }
        Request request = builder.url(url).post(body).build();
        enqueue(client().newCall(request), responseClazz, httpCallback);
    }

    private <T> void enqueue(Call call, Class<T> responseClazz, IHttpCallback<T> httpCallback) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                BaseUtil.logD(e.getMessage());
                runOnUiThread(() -> {
                    httpCallback.onFail(-1, e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                try {
                    String dataJson = Objects.requireNonNull(response.body()).string();
                    BaseUtil.logD(dataJson);
                    T t = GsonTool.jsonStringToObj(dataJson, responseClazz);
                    runOnUiThread(() -> {
                        httpCallback.onSuccess(dataJson, t);
                    });
                } catch (Exception e) {
                    BaseUtil.logD(e.getMessage());
                    runOnUiThread(() -> {
                        httpCallback.onFail(-1, e.getMessage());
                    });
                }
            }
        });
    }

    public interface IHttpCallback<T> {

        void onSuccess(String bodyString, T data);

        void onFail(int code, String message);
    }
}
