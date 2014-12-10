package io.relayr.tellmewhen.util;

import java.io.Serializable;

import io.relayr.model.DeviceModel;

public enum SensorType implements Serializable {

    TEMPERATURE("°C", DeviceModel.TEMPERATURE_HUMIDITY),
    HUMIDITY("%", DeviceModel.TEMPERATURE_HUMIDITY),
    NOISE_LEVEL("%", DeviceModel.MICROPHONE),
    PROXIMITY("%", DeviceModel.LIGHT_PROX_COLOR),
    LUMINOSITY("%", DeviceModel.LIGHT_PROX_COLOR);
//    ACCELERATION("", DeviceModel.ACCELEROMETER_GYROSCOPE);

    private final DeviceModel model;
    private final String unit;

    SensorType(String unit, DeviceModel model) {
        this.unit = unit;
        this.model = model;
    }

    public DeviceModel getModel() {
        return model;
    }

    public static SensorType getByName(String name) {
        if (name == null)
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
            if (type.ordinal() == pos)
                return type;
        }
        return null;
    }
}