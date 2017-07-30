package com.arvis.android.upandgo;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Jack on 29/7/17.
 */

public class Config {

    private static SharedPreferences config;

    private static final String PROFILE = "upAndGoSetting";

    public static final String HOME_LAT = "homeLat";

    public static final String HOME_LON = "homeLon";

    public static final String WORK_LAT = "workLat";

    public static final String WORK_LON = "workLon";

    public static final String CUR_LOC_LAT = "curLocLat";

    public static final String CUR_LOC_LON = "curLocLon";

    public static final String DES_LOC_LAT = "destLocLat";

    public static final String DES_LOC_LON = "destLocLon";

    public static void init(Context context){

        config = context.getSharedPreferences(PROFILE, Context.MODE_PRIVATE);
    }

    public static void addConfigString(String key, String value){

        config.edit().putString(key, value).commit();
    }

    public static void addFloatConfig(String key, float value){

        config.edit().putFloat(key, value).commit();
    }

    public static String getStringConfig(String key){

        return config.getString(key, null);
    }

    public static float getFloatConfig(String key){

        return config.getFloat(key, 0);
    }
}
