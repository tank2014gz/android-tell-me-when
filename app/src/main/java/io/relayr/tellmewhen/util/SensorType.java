package io.relayr.tellmewhen.util;

import java.io.Serializable;

import io.relayr.model.DeviceModel;

public enum SensorType implements Serializable{

    TEMP("temperature", "°C", DeviceModel.TEMPERATURE_HUMIDITY),
    HUM("humidity", "%", DeviceModel.TEMPERATURE_HUMIDITY),
    NOISE("noise", "%", DeviceModel.MICROPHONE),
    PROX("proximity", "%", DeviceModel.LIGHT_PROX_COLOR),
    LIGHT("light", "%", DeviceModel.LIGHT_PROX_COLOR),
    ACC("acceleration", "G", DeviceModel.ACCELEROMETER_GYROSCOPE);

    private final String name;
    private final DeviceModel model;
    private final String unit;

    SensorType(String name, String unit, DeviceModel model) {
        this.name = name;
        this.unit = unit;
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public DeviceModel getModel() {
        return model;
    }

    public static SensorType getByName(String name) {
        for (SensorType sensorType : values()) {
            if (sensorType.getName().equals(name)) return sensorType;
        }

        return null;
    }

    public String getUnit() {
        return unit;
    }
}