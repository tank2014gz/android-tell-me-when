package io.relayr.tellmewhen.util;

public enum SensorType {
    TEMP("temperature"), HUM("humidity"), NOISE("noise"), PROX("proximity"), LIGHT("light"), ACC("acceleration");

    private final String name;

    SensorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static SensorType getByName(String name) {
        for (SensorType sensorType : values()) {
            if (sensorType.getName().equals(name)) return sensorType;
        }

        return null;
    }
}