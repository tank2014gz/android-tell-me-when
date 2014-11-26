package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Notification")
public class Notification extends Model {

    @Column(name = "ruleName") private String ruleName;
    @Column(name = "ruleValue") private String value;
    @Column(name = "timestamp") private long timestamp;
}
