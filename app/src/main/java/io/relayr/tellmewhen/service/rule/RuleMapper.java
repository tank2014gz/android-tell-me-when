package io.relayr.tellmewhen.service.rule;

import io.relayr.tellmewhen.model.Rule;
import io.relayr.tellmewhen.storage.Storage;

public class RuleMapper {

    public static DbRule toDbRule(Rule rule) {
        DbRule dbRule = new DbRule(Storage.loadUserId(), rule.transmitterId, rule.sensorId);

        DbRule.Details details = new DbRule.Details(rule.isNotifying);
        dbRule.setDetails(details);

        DbRule.Condition condition = new DbRule.Condition(rule.getSensorType().name().toLowerCase(),
                rule.getOperatorType().getValue(), rule.value);
        dbRule.setCondition(condition);

        DbRule.Notification notif = new DbRule.Notification("gcm", "example@example.com");
        dbRule.setNotification(notif);

        return dbRule;
    }


}
