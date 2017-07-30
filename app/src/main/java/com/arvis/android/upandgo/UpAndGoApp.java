package com.arvis.android.upandgo;

import android.app.Application;

/**
 * Created by Jack on 29/7/17.
 */

public class UpAndGoApp extends Application{

    @Override
    public void onCreate() {

        super.onCreate();

        Config.init(this);
    }
}
