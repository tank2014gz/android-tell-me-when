package io.relayr.tellmewhen.util;

import io.relayr.RelayrSdk;

public class LogUtil {

    public static final String PREFIX = "TMW";

    public static final String CREATE_RULE_TRANSMITTER = "CREATE rule - transmitter screen";
    public static final String CREATE_RULE_SENSOR = "CREATE rule - sensor screen";
    public static final String CREATE_RULE_THRESHOLD = "CREATE rule - threshold screen";
    public static final String CREATE_RULE_NAME = "CREATE rule - name screen";
    public static final String CREATE_RULE_CANCEL = "CREATE rule - cancelled";
    public static final String CREATE_RULE_FINISH = "CREATE rule - finished";
    public static final String CREATE_RULE_FINISHED = "CREATED rule - sensor: %s, threshold: %s";

    public static final String EDIT_RULE_NOTIFYING = "EDIT rule - notifying changed";
    public static final String EDIT_RULE_TRANSMITTER = "EDIT rule - changing transmitter";
    public static final String EDIT_RULE_SENSOR = "EDIT rule - changing sensor";
    public static final String EDIT_RULE_THRESHOLD = "EDIT rule - changing threshold";
    public static final String EDIT_RULE_NAME = "EDIT rule - changing name";
    public static final String EDIT_RULE_CANCEL = "EDIT rule - cancelled";
    public static final String EDIT_RULE_FINISH = "EDIT rule - finished";

    public static final String DELETE_RULE = "DELETED rule - sensor: %s";
    public static final String DELETE_NOTIFICATION = "DELETED notification";
    public static final String DELETE_ALL_NOTIFICATIONS = "DELETED all %s notifications";

    public static final String VIEW_APP = "VIEW app";
    public static final String VIEW_WITH_PUSH = "VIEW push notification";

    public static void logMessage(String msg) {
        RelayrSdk.logMessage(PREFIX + ": " + msg);
    }
}
