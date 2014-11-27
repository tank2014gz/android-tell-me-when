package io.relayr.tellmewhen.service.rule;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DbRule implements Serializable {

    @SerializedName("user_id") private String userId;
    @SerializedName("tx_id") private String transmitterId;
    @SerializedName("dev_id") private String deviceId;
    @SerializedName("details") private Details details;
    @SerializedName("condition") private Condition condition;
    @SerializedName("notification") private Notification notification;

    static class Details implements Serializable{

        @SerializedName("active")  private  boolean active;

        public Details(boolean active){
            this.active = active;
        }
    }

    static class Condition implements Serializable{

        @SerializedName("sensor")  private  String sensor;
        @SerializedName("op")  private  String operator;
        @SerializedName("val")  private  int value;

        Condition(String sensor, String operator, int value) {
            this.sensor = sensor;
            this.operator = operator;
            this.value = value;
        }
    }

    static class Notification implements Serializable {

        @SerializedName("type") private String type;
        @SerializedName("address") private String address;

        Notification(String type, String address) {
            this.type = type;
            this.address = address;
        }
    }

    public DbRule(String userId, String transmitterId, String deviceId) {
        this.userId = userId;
        this.transmitterId = transmitterId;
        this.deviceId = deviceId;
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public void setNotification(Notification notification) {
        this.notification = notification;
    }
}
