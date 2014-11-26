package io.relayr.tellmewhen.model;

import java.io.Serializable;

import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;

public class Rule implements Serializable {

    private boolean isNotifying;

    private String name;
    private String transmitterId;
    private String transmitterType;
    private String transmitterName;
    private SensorType sensorType;
    private OperatorType operatorType;
    private Integer value;
    private String sensorId;

    public Rule() {
        this.transmitterType = "Relayr WunderBar";
        this.isNotifying = true;
    }

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

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public void setTransmitterId(String transmitterId) {
        this.transmitterId = transmitterId;
    }

    public String getTransmitterId() {
        return transmitterId;
    }

    public void setSensorId(String sensorId) {
        this.sensorId = sensorId;
    }

    public String getSensorId() {
        return sensorId;
    }
}
