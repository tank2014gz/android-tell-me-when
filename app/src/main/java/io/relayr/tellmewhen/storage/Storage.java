package io.relayr.tellmewhen.storage;

import android.content.Context;
import android.content.SharedPreferences;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;

public class Storage {

    private static final String NAME = "Storage";
    private static final String USER_ID = "user.id";
    private static final String TRANSMITTER_CONTROL = "transmitter.control";

    private static final String RULE_NOTIFYING = "rule.notifying";
    private static final String RULE_NAME = "rule.name";
    private static final String RULE_TRANS_ID = "rule.trans.id";
    private static final String RULE_TRANS_NAME = "rule.trans.name";
    private static final String RULE_TRANS_TYPE = "rule.trans.type";
    private static final String RULE_SENSOR = "rule.sensor";
    private static final String RULE_SENSOR_ID = "rule.sensor.id";
    private static final String RULE_OPERATOR = "rule.operator";
    private static final String RULE_VALUE = "rule.value";

    private static SharedPreferences sStorage = null;
    private static boolean sEditingRule = false;

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

    public static void saveRuleName(String name) {
        save(RULE_NAME, name);
    }

    public static void saveRuleTransId(String transId) {
        save(RULE_TRANS_ID, transId);
    }

    public static void saveRuleTransName(String name) {
        save(RULE_TRANS_NAME, name);
    }

    public static void saveRuleTransType(String type) {
        save(RULE_TRANS_TYPE, type);
    }

    public static void saveRuleSensor(SensorType type) {
        save(RULE_SENSOR, type.getName());
    }

    public static void saveRuleSensorId(String sensorId) {
        save(RULE_SENSOR_ID, sensorId);
    }

    public static void saveRuleOperator(OperatorType type) {
        save(RULE_OPERATOR, type.getName());
    }

    public static void saveRuleNotifyState(boolean state) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putBoolean(RULE_NOTIFYING, state);
        editor.apply();
    }

    public static void saveRuleValue(int value) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putInt(RULE_VALUE, value);
        editor.apply();
    }

    public static String loadRuleName() {
        return sStorage.getString(RULE_NAME, null);
    }

    public static String loadRuleTransId() {
        return sStorage.getString(RULE_TRANS_ID, null);
    }

    public static String loadRuleTransName() {
        return sStorage.getString(RULE_TRANS_NAME, null);
    }

    public static String loadRuleTransType() {
        return sStorage.getString(RULE_TRANS_TYPE, null);
    }

    public static SensorType loadRuleSensor() {
        return SensorType.getByName(sStorage.getString(RULE_SENSOR, null));
    }

    public static String loadRuleSensorId() {
        return sStorage.getString(RULE_SENSOR_ID, null);
    }

    public static OperatorType loadRuleOperator() {
        return OperatorType.getByName(sStorage.getString(RULE_OPERATOR, null));
    }

    public static int loadRuleValue() {
        return sStorage.getInt(RULE_VALUE, 0);
    }

    public static boolean loadRuleNotifyState() {
        return sStorage.getBoolean(RULE_NOTIFYING, true);
    }

    public static void save(String path, String value) {
        SharedPreferences.Editor editor = sStorage.edit();
        editor.putString(path, value);
        editor.apply();
    }

    public static Rule composeRule() {
        Rule r = new Rule(loadRuleTransName());
        r.setTransmitterId(loadRuleTransId());
        r.setTransmitterType(loadRuleTransType());
        r.setOperatorType(loadRuleOperator());
        r.setSensorType(loadRuleSensor());
        r.setSensorId(loadRuleSensorId());
        r.setValue(loadRuleValue());
        r.setName(loadRuleName());
        r.setNotifying(loadRuleNotifyState());

        return r;
    }

    public static void prepareRuleForEdit(Rule rule) {
        saveRuleName(rule.getName());
        saveRuleNotifyState(rule.isNotifying());
        saveRuleTransId(rule.getTransmitterId());
        saveRuleTransType(rule.getTransmitterType());
        saveRuleTransName(rule.getTransmitterName());
        saveRuleSensor(rule.getSensorType());
        saveRuleSensorId(rule.getSensorId());
        saveRuleOperator(rule.getOperatorType());
        saveRuleValue(rule.getValue());
    }

    public static boolean isRuleEditing() {
        return sEditingRule;
    }

    public static void setRuleEditing(boolean sEditingRule) {
        Storage.sEditingRule = sEditingRule;

    }
}
