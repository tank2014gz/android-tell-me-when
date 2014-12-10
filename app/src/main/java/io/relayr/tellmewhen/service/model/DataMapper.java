package io.relayr.tellmewhen.service.model;

import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.model.TMWNotification;
import io.relayr.tellmewhen.model.TMWRule;
import io.relayr.tellmewhen.storage.Storage;
import io.relayr.tellmewhen.util.SensorUtil;

import static io.relayr.tellmewhen.service.model.DbRule.Condition;
import static io.relayr.tellmewhen.service.model.DbRule.Notification;

public class DataMapper {

    public static DbRule toDbRule(TMWRule rule) {
        DbRule dbRule = new DbRule(Storage.loadUserId(), rule.transmitterId, rule.sensorId, rule.isNotifying);

        DbRule.Details details = new DbRule.Details(rule.name);
        dbRule.setDetails(details);

        Condition condition = new Condition(rule.getSensorType().name().toLowerCase(),
                rule.getOperatorType().getValue(),
                SensorUtil.scaleToServerData(rule.getSensorType(), rule.value));
        dbRule.setCondition(condition);

        Notification notif = new Notification("gcm", Storage.loadGmsRegistrationId());
        dbRule.addNotification(notif);

        return dbRule;
    }

    public static TMWNotification toRuleNotification(DbNotification dbNotif) {
        TMWNotification ruleNotif = new TMWNotification();
        ruleNotif.ruleId = dbNotif.getRuleId();
        ruleNotif.dbId = dbNotif.getDbId();
        ruleNotif.drRev = dbNotif.getDrRev();
        ruleNotif.value = dbNotif.getValue();
        ruleNotif.timestamp = dbNotif.getTimestamp();

        return ruleNotif;
    }

    public static TMWRule toRule(DbRule dbRule) {
        TMWRule rule = new TMWRule();
        rule.dbId = dbRule.getId();
        rule.drRev = dbRule.getRev();

        rule.isNotifying = dbRule.isActive();
        rule.name = dbRule.getDetails().getName();

        rule.transmitterId = dbRule.getTransmitterId();
        rule.transmitterType = getTransmitterName(dbRule.getTransmitterId());
        rule.transmitterName = "Relayr Wunderbar";

        rule.sensorId = dbRule.getSensorId();
        rule.sensorType = dbRule.getCondition().getSensor().ordinal();

        rule.operatorType = dbRule.getCondition().getOperator().ordinal();

        rule.value = SensorUtil.scaleToUiData(rule.getSensorType(), dbRule.getCondition().getValue());

        return rule;
    }

    private static String getTransmitterName(String transmitterId) {
        for (Transmitter transmitter : Storage.loadTransmitters()) {
            if (transmitter.id.equals(transmitterId)) {
                return transmitter.getName();
            }
        }

        return "";
    }
}
