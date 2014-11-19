package io.relayr.tellmewhen.model;

import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;

public class Rule {

    private boolean isNotifying;

    private String name;
    private String transmitterType;
    private String transmitterName;
    private SensorType sensorType;
    private OperatorType operatorType;
    private float value;

    public Rule(String transmitterName) {
        this.transmitterName = transmitterName;

        this.transmitterType = "Relayr WunderBar";
        this.isNotifying = true;
    }

    public boolean isNotifying() {
        return isNotifying;
    }

    public void setNotifying(boolean isNotifying) {
        this.isNotifying = isNotifying;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransmitterType() {
        return transmitterType;
    }

    public void setTransmitterType(String transmitterType) {
        this.transmitterType = transmitterType;
    }

    public String getTransmitterName() {
        return transmitterName;
    }

    public void setTransmitterName(String transmitterName) {
        this.transmitterName = transmitterName;
    }

    public SensorType getSensorType() {
        return sensorType;
    }

    public void setSensorType(SensorType sensorType) {
        this.sensorType = sensorType;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
