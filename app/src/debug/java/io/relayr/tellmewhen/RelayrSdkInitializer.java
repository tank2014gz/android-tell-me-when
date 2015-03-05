package io.relayr.tellmewhen;

import android.content.Context;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.relayr.RelayrSdk;

public abstract class RelayrSdkInitializer {

    static void initSdk(Context context) {
        new RelayrSdk.Builder(context).inMockMode(true).build();
        Fabric.with(context, new Crashlytics());
    }
}
