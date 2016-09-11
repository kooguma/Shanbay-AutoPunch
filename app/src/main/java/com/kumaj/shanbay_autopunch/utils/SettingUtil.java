package com.kumaj.shanbay_autopunch.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.kumaj.shanbay_autopunch.App;
import com.kumaj.shanbay_autopunch.mode.SettingModel;

public final class SettingUtil {

    private final static String USER_SETTINGS = "SETTING_PREFERENCE";
    private final static String USER_SETTINGS_KEY = "SETTING_KEY";
    private static SettingModel DEFAULT_MODEL ;

    private SettingUtil(){
        DEFAULT_MODEL = new SettingModel();
        DEFAULT_MODEL.setExpectedTime(5 * 60 * 1000);
        DEFAULT_MODEL.setAutoPunch(true);
        DEFAULT_MODEL.setAutoShare(false);
    }

    public static SettingUtil getInstance(){
        return InstanceHolder.instance;
    }

    public static class InstanceHolder{
        static final SettingUtil instance = new SettingUtil();
    }

    public  void saveSettings(SettingModel settings) {
        if(settings == null) return;
        SharedPreferences.Editor editor =
            App.getsContext().getSharedPreferences(USER_SETTINGS, Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        editor.putString(USER_SETTINGS_KEY,gson.toJson(settings));
        editor.apply();
    }

    public SettingModel loadSettings(){
        SharedPreferences userPreference =
            App.getsContext().getSharedPreferences(USER_SETTINGS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = userPreference.getString(USER_SETTINGS_KEY, null);
        if (json != null) {
           return gson.fromJson(json, SettingModel.class);
        }
        return DEFAULT_MODEL;
    }
}
