package io.relayr.tellmewhen;

import android.app.Application;

import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

public class TellMeWhenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        RelayrSdkInitializer.initSdk(this);

        Storage.init(getApplicationContext());
        SensorUtil.init(getApplicationContext());
    }
}
