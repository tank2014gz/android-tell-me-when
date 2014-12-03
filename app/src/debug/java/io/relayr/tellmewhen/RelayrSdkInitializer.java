package io.relayr.tellmewhen;

import android.content.Context;

import io.relayr.RelayrSdk;

public abstract class RelayrSdkInitializer {

    static void initSdk(Context context) {
        RelayrSdk.init(context);
    }
}
