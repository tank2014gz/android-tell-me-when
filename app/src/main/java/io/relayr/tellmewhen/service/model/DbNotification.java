package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class DbNotification implements Serializable {

    @SerializedName("_id") private String dbId;
    @SerializedName("_rev") private String drRev;

    @SerializedName("user_id") private String userId;
    @SerializedName("rule_id") private String ruleId;
    @SerializedName("val") private int value;
    @SerializedName("timestamp") private String timestamp;

    public DbNotification() {
    }

    public String getDbId() {
        return dbId;
    }

    public void setDbId(String dbId) {
        this.dbId = dbId;
    }

    public String getDrRev() {
        return drRev;
    }

    public void setDrRev(String drRev) {
        this.drRev = drRev;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
