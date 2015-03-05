package io.relayr.tellmewhen;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

import com.crashlytics.android.Crashlytics;
import dagger.ObjectGraph;
import io.fabric.sdk.android.Fabric;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

public class TellMeWhenApplication extends Application {

    public static ObjectGraph objectGraph;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        RelayrSdkInitializer.initSdk(this);

        ActiveAndroid.initialize(this);

        Storage.init(getApplicationContext());
        SensorUtil.init(getApplicationContext());

        objectGraph = ObjectGraph.create(new AppModule(getApplicationContext()));
        objectGraph.injectStatics();
    }

    @Override
    public void onTerminate() {
        ActiveAndroid.dispose();
        super.onTerminate();
    }


}
