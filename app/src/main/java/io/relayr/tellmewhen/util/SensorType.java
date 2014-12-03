package io.relayr.tellmewhen.util;

import java.io.Serializable;

import io.relayr.model.DeviceModel;

public enum SensorType implements Serializable{

    TEMP("temperature", "°C", DeviceModel.TEMPERATURE_HUMIDITY),
    HUM("humidity", "%", DeviceModel.TEMPERATURE_HUMIDITY),
    SND_LEVEL("noise", "%", DeviceModel.MICROPHONE),
    PROX("proximity", "%", DeviceModel.LIGHT_PROX_COLOR),
    LIGHT("light", "%", DeviceModel.LIGHT_PROX_COLOR),
    ACCEL("acceleration", "", DeviceModel.ACCELEROMETER_GYROSCOPE);

    private final String title;
    private final DeviceModel model;
    private final String unit;

    SensorType(String title, String unit, DeviceModel model) {
        this.title = title;
        this.unit = unit;
        this.model = model;
    }

    public String getTitle() {
        return title;
    }

    public DeviceModel getModel() {
        return model;
    }

    public static SensorType getByName(String name) {
        if(name == null)
            return null;

        for (SensorType sensorType : values()) {
            if (sensorType.name().toLowerCase().equals(name.toLowerCase())) return sensorType;
        }

        return null;
    }

    public String getUnit() {
        return unit;
    }

    public static SensorType byId(int pos) {
        for (SensorType type : values()) {
            if(type.ordinal() == pos)
                return type;
        }
        return null;
    }
}