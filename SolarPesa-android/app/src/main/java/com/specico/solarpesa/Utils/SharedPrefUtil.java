package com.specico.solarpesa.Utils;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefUtil {

    public static String getStringPreference(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return preferences.getString(key, "");
    }

    public static void clearPreferences(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    public static void setStringPreference(Context context, String key, String value) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public static void setBooleanPreference(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public static boolean getBooleanPreference(Context context, String key) {
        SharedPreferences preferences = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }
}
