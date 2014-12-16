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
public class TMWNotification extends Model {

    @Column(name = "dbId") public String dbId;
    @Column(name = "dbRev") public String drRev;

    @Column(name = "userId") public String userId;
    @Column(name = "ruleId") public String ruleId;
    @Column(name = "value") public float value;
    @Column(name = "timestamp") public String timestamp;

    public TMWNotification() {
    }

    public long getTimestamp() throws ParseException {
        return Long.parseLong(timestamp);
    }
}
