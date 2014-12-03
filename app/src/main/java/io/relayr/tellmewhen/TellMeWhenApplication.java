package io.relayr.tellmewhen;

import com.activeandroid.ActiveAndroid;
import com.activeandroid.app.Application;

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

        ActiveAndroid.initialize(this);

        RelayrSdkInitializer.initSdk(this);

        Storage.init(getApplicationContext());
        SensorUtil.init(getApplicationContext());

        objectGraph = ObjectGraph.create(new AppModule());
        objectGraph.injectStatics();
    }
}
