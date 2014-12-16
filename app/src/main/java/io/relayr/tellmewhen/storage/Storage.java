package io.relayr.tellmewhen.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.consts.SensorType;

public class Storage {

    private static final String NAME = "Storage";
    private static final String USER_ID = "user.id";
    private static final String USER_ONBOADRED = "user.onboarded";
    private static final String START_SCREEN = "start.screen";
    private static final String NOTIFICATION_VISIBILITY = "notifications.visibility";

    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_OLD_REG_ID = "old_registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static SharedPreferences sStorage = null;

    private static TMWRule createRule = null;
    private static Pair<String, SensorType> originalSensor = null;
    private static List<Transmitter> sTransmitters = new ArrayList<>();
    private static TMWNotification mNotificationDetails;

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

    public static boolean isUserOnBoarded() {
        return sStorage.getBoolean(USER_ONBOADRED, false);
    }

    public static void save(String path, String value) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putString(path, value);
        editor.apply();
    }

    public static void prepareRuleForCreate() {
        createRule = new TMWRule();
        createRule.modified = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();
    }

    public static TMWRule getRule() {
        return createRule;
    }

    public static Pair<String, SensorType> getOriginalSensor() {
        return originalSensor;
    }

    public static void prepareRuleForEdit(TMWRule rule) {
        createRule = rule;
        createRule.modified = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

        originalSensor = new Pair<>(rule.sensorId, rule.getSensorType());
    }

    public static boolean isRuleEditing() {
        return originalSensor != null;
    }

    public static void saveGmsRegId(String regId) {
        if (loadGmsRegId() != null && regId != null && !regId.equals(loadGmsRegId()))
            save(PROPERTY_OLD_REG_ID, loadGmsRegId());
        save(PROPERTY_REG_ID, regId);
    }

    public static void saveGmsAppVersion(int appVersion) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    public static String loadGmsRegId() {
        return sStorage.getString(PROPERTY_REG_ID, null);
    }

    public static String loadOldGmsRegId() {
        return sStorage.getString(PROPERTY_OLD_REG_ID, null);
    }

    public static int loadGmsAppVersion() {
        return sStorage.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
    }

    public static void clearRuleData() {
        createRule = null;
        originalSensor = null;
    }

    public static void saveTransmitters(List<Transmitter> transmitters) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(USER_ONBOADRED, !transmitters.isEmpty());
        editor.apply();

        sTransmitters = transmitters;
    }

    public static List<Transmitter> loadTransmitters() {
        return sTransmitters;
    }

    public static void startRuleScreen(boolean rules) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(START_SCREEN, rules);
        editor.apply();
    }

    public static boolean isStartScreenRules() {
        return sStorage.getBoolean(START_SCREEN, true);
    }

    public static void showNotification(TMWNotification notification) {
        mNotificationDetails = notification;
    }

    public static TMWNotification getNotificationDetails() {
        return mNotificationDetails;
    }

    public static void setNotificationScreeVisible(boolean visible) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(NOTIFICATION_VISIBILITY, visible);
        editor.apply();
    }

    public static boolean isNotificationScreenVisible() {
        return sStorage.getBoolean(NOTIFICATION_VISIBILITY, false);
    }
}
