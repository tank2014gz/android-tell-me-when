package io.relayr.tellmewhen.service.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.relayr.tellmewhen.consts.OperatorType;
import io.relayr.tellmewhen.consts.SensorType;

public class DbRule implements Serializable {

    @SerializedName("_id") private String id;
    @SerializedName("_rev") private String rev;

    @SerializedName("user_id") private String userId;
    @SerializedName("tx_id") private String transmitterId;
    @SerializedName("dev_id") private String sensorId;
    @SerializedName("active") private boolean active;
    @SerializedName("details") private Details details;
    @SerializedName("condition") private Condition condition;
    @SerializedName("notifications") private List<Notification> notifications = new ArrayList<Notification>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRev() {
        return rev;
    }

    public void setRev(String revID) {
        this.rev = rev;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTransmitterId() {
        return transmitterId;
    }

    public void setTransmitterId(String transmitterId) {
        this.transmitterId = transmitterId;
    }

    public String getSensorId() {
        return sensorId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Details getDetails() {
        return details;
    }

    public Condition getCondition() {
        return condition;
    }

    public List<Notification> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public DbRule(String userId, String transmitterId, String sensorId, boolean active) {
        this.userId = userId;
        this.transmitterId = transmitterId;
        this.sensorId = sensorId;
        this.active = active;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void addNotification(Notification notification) {
        this.notifications.add(notification);
    }

    public static class Details implements Serializable {

        @SerializedName("name") private String name;
        @SerializedName("modified") private long modified;

        public Details(String name, long modified) {
            this.name = name;
            this.modified = modified;
        }

        public String getName() {
            return name;
        }

        public long getModified() {
            return modified;
        }
    }

    public static class Condition implements Serializable {

        @SerializedName("meaning") private String sensor;
        @SerializedName("op") private String operator;
        @SerializedName("val") private float value;

        public Condition(String sensor, String operator, float value) {
            this.sensor = sensor;
            this.operator = operator;
            this.value = value;
        }

        public SensorType getSensor() {
            return SensorType.getByName(sensor);
        }

        public OperatorType getOperator() {
            return OperatorType.getByValue(operator);
        }

        public float getValue() {
            return value;
        }
    }

    public static class Notification implements Serializable {

        @SerializedName("type") private String type;
        @SerializedName("key") private String key;

        public Notification(String type, String key) {
            this.type = type;
            this.key = key;
        }

        public String getKey() {
            return key;
        }
        public String getType() {
            return type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Notification that = (Notification) o;

            if (!key.equals(that.key)) return false;
            if (!type.equals(that.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + key.hashCode();
            return result;
        }
    }
}
