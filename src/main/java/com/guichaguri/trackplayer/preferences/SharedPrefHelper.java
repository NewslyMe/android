package com.guichaguri.trackplayer.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.guichaguri.trackplayer.module.NewsItem;
import com.guichaguri.trackplayer.module.NewsItemStorage;

public class SharedPrefHelper {

    // save data in sharedPrefences
    public static void setSharedOBJECT(Context context, String key,
                                       Object value) {

        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(value);
        prefsEditor.putString(key, json);
        prefsEditor.commit();
        prefsEditor.apply();
    }

    // get data from sharedPrefences
    public static Object getSharedOBJECT(Context context, String key) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        Gson gson = new Gson();
        String json = sharedPreferences.getString(key, "");
        Object obj = gson.fromJson(json, Object.class);
        if(obj != null && obj != "") {
            NewsItemStorage objData = new Gson().fromJson(obj.toString(), NewsItemStorage.class);
            return objData;
        }else{
            return null;
        }
    }

    public static float getSpeed(Context context){
        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);
        float speedFloat = sharedPreferences.getFloat("speed", 1.0f);
        return speedFloat;

    }

    public static void setSpeed(Context context, float speed){
        SharedPreferences sharedPreferences =  context.getSharedPreferences(
                context.getPackageName(), Context.MODE_PRIVATE);

        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        prefsEditor.putFloat("speed", speed);
        prefsEditor.commit();
    }

}
