package io.relayr.tellmewhen.service.rule;

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


}
