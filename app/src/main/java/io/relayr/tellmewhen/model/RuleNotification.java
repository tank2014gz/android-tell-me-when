package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Notification")
public class RuleNotification extends Model {

    @Column(name = "ruleName") public String name;
    @Column(name = "ruleValue") public String value;
    @Column(name = "timestamp") public long timestamp;
}
