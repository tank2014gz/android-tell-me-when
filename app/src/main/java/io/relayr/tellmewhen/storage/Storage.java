package io.relayr.tellmewhen.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {

    private static final String NAME = "Storage";
    private static SharedPreferences mStorage = null;

    private static final String WUNDERBAR_ID = "wunderbar.id";
    private static final String WUNDERBAR_NAME = "wunderbar.name";
    private static final String MEASUREMENT_NAME = "maesurement.name";

    public static void init(Context applicationContext) {
        new Storage(applicationContext);
    }

    private Storage(Context context) {
        mStorage = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void setWunderbarName(String wunderbarName) {
        SharedPreferences.Editor editor = mStorage.edit();
        editor.putString(WUNDERBAR_NAME, wunderbarName);
        editor.apply();
    }

    public static String getWunderbarName() {
        return mStorage.getString(WUNDERBAR_NAME, null);
    }
}
