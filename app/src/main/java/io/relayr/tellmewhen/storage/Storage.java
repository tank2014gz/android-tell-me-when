package io.relayr.tellmewhen.storage;

import android.content.Context;
import android.content.SharedPreferences;

public class Storage {

    private static final String NAME = "Storage";
    private static final String WUNDERBAR_NAME = "wunderbar.name";
    private static final String MEASUREMENT_NAME = "maesurement.name";
    private static final String USER_ID = "user.id";

    private static final String TRANSMITTER_CONTROL = "transmitter.control";

    private static SharedPreferences sStorage = null;

    public static void init(Context applicationContext) {
        new Storage(applicationContext);
    }

    private Storage(Context context) {
        sStorage = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void saveWunderbarName(String wunderbarName) {
        save(WUNDERBAR_NAME, wunderbarName);
    }

    public static String loadWunderbarName() {
        return sStorage.getString(WUNDERBAR_NAME, null);
    }

    public static void saveMeasurement(String measurement) {
        save(MEASUREMENT_NAME, measurement);
    }

    public static String loadMeasurement() {
        return sStorage.getString(MEASUREMENT_NAME, null);
    }

    public static void saveUserId(String id) {
        save(USER_ID, id);
    }

    public static String loadUserId(){
        return sStorage.getString(USER_ID, null);
    }

    public static void saveTransmiterState(boolean exist){
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(TRANSMITTER_CONTROL, exist);
        editor.apply();
    }

    public static boolean transmitterExists(){
        return sStorage.getBoolean(TRANSMITTER_CONTROL, false);
    }

    private static void save(String key, String value) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
