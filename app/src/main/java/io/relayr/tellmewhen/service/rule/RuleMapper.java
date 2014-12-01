package io.relayr.tellmewhen.service.rule;

import io.relayr.model.Transmitter;
import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.storage.Storage;

public class RuleMapper {

    public static DbRule toDbRule(Rule rule) {
        DbRule dbRule = new DbRule(Storage.loadUserId(), rule.transmitterId, rule.sensorId, rule.isNotifying);

        DbRule.Details details = new DbRule.Details(rule.name);
        dbRule.setDetails(details);

        DbRule.Condition condition = new DbRule.Condition(rule.getSensorType().name().toLowerCase(),
                rule.getOperatorType().getValue(), rule.value);
        dbRule.setCondition(condition);

        DbRule.Notification notif = new DbRule.Notification("gcm", Storage.loadGmsRegistrationId());
        dbRule.addNotification(notif);

        return dbRule;
    }


    public static Rule toRule(DbRule dbRule) {
        Rule rule = new Rule();
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

        rule.value = dbRule.getCondition().getValue();
        return rule;
    }

    private static String getTransmitterName(String transmitterId){
        for (Transmitter transmitter : Storage.loadTransmitters()) {
            if(transmitter.id.equals(transmitterId)){
                return transmitter.getName();
            }
        }

        return "";
    }
}
