package io.relayr.tellmewhen.storage;

import android.content.Context;
import android.content.SharedPreferences;

import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.model.Rule;

public class Storage {

    private static final String NAME = "Storage";
    private static final String USER_ID = "user.id";
    private static final String TRANSMITTER_CONTROL = "transmitter.control";

    private static SharedPreferences sStorage = null;

    private static Rule currentRule = null;

    private Storage(Context context) {
        sStorage = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    public static void init(Context applicationContext) {
        new Storage(applicationContext);
    }

    public static void saveUserId(String id) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putString(USER_ID, id);
        editor.apply();
    }

    public static String loadUserId() {
        return sStorage.getString(USER_ID, null);
    }

    public static void saveTransmiterState(boolean exist) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(TRANSMITTER_CONTROL, exist);
        editor.apply();
    }

    public static boolean transmitterExists() {
        return sStorage.getBoolean(TRANSMITTER_CONTROL, false);
    }

    public static void createRule(Transmitter trans) {
        currentRule = new Rule(trans.getName());
    }

    public static Rule getCurrentRule() {
        return currentRule;
    }
}
