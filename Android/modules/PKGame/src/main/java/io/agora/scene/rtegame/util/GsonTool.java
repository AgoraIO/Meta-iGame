package io.agora.scene.rtegame.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import org.json.JSONObject;

import java.lang.reflect.Type;

public class GsonTool {
    private static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE).create();

    public static String objToJsonString(Object obj) {
        return gson.toJson(obj);
    }

    public static <T> T jsonStringToObj(String jsonString, Class<T> clazz) {
        try {
            return gson.fromJson(jsonString, clazz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T toObject(String jsonString, Type type) {
        try {
            return gson.fromJson(jsonString, type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean getBoolean(JSONObject jsonObject, String valueKey, Boolean defaultValue) {
        if (jsonObject == null || valueKey == null) return defaultValue;
        boolean value = defaultValue;
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getBoolean(valueKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static int getInt(JSONObject jsonObject, String valueKey) {
        if (jsonObject == null || valueKey == null) return 0;
        int value = 0;
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getInt(valueKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static long getLong(JSONObject jsonObject, String valueKey) {
        if (jsonObject == null || valueKey == null) return 0L;
        long value = 0L;
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getLong(valueKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getString(JSONObject jsonObject, String valueKey) {
        if (jsonObject == null || valueKey == null) return "";
        String value = "";
        if (jsonObject.has(valueKey)) try {
            value = jsonObject.getString(valueKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
