package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Table(name = "Notifications")
public class TMWNotification extends Model implements Serializable {

    @Column(name = "dbId") @SerializedName("_id") public String dbId;
    @Column(name = "dbRev") @SerializedName("_rev") public String drRev;

    @Column(name = "userId") @SerializedName("user_id") public String userId;
    @Column(name = "ruleId") @SerializedName("rule_id") public String ruleId;
    @Column(name = "value") @SerializedName("val") public float value;
    @Column(name = "timestamp") @SerializedName("timestamp") public String timestamp;

    public TMWNotification() {
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public long getTimestamp() throws ParseException {
        return Long.parseLong(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
