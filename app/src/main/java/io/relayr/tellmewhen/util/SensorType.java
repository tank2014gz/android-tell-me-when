package io.relayr.tellmewhen.util;

import io.relayr.model.DeviceModel;

public enum SensorType {

    TEMP("temperature", DeviceModel.TEMPERATURE_HUMIDITY),
    HUM("humidity", DeviceModel.TEMPERATURE_HUMIDITY),
    NOISE("noise", DeviceModel.MICROPHONE),
    PROX("proximity", DeviceModel.LIGHT_PROX_COLOR),
    LIGHT("light", DeviceModel.LIGHT_PROX_COLOR),
    ACC("acceleration", DeviceModel.ACCELEROMETER_GYROSCOPE);

    private final String name;
    private final DeviceModel model;

    SensorType(String name, DeviceModel model) {
        this.name = name;
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
}