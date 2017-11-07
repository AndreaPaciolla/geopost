package com.pacho.geopost.application;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

/**
 * Created by do_ma on 05/11/2017.
 */
public class GeopostApplication extends Application {

    private static GeopostApplication mInstance;
    private static Context mAppContext;

    public static GeopostApplication getInstance(){
        return mInstance;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        this.setAppContext(getApplicationContext());
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public void setAppContext(Context mAppContext) {
        this.mAppContext = mAppContext;
    }

    public static Context getAppContext() {
        return mAppContext;
    }

}
