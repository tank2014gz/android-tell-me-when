package io.relayr.tellmewhen;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;

import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

public class TellMeWhenApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ActiveAndroid.initialize(this);

        RelayrSdkInitializer.initSdk(this);

        Storage.init(getApplicationContext());
        SensorUtil.init(getApplicationContext());
    }
}
