package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.io.Serializable;

import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;

@Table(name = "Rule")
public class Rule extends Model {

    @Column(name = "isNotifying") public boolean isNotifying;
    @Column(name = "name") public String name;
    @Column(name = "transmitterId") public String transmitterId;
    @Column(name = "transmitterType") public String transmitterType;
    @Column(name = "transmitterName") public String transmitterName;
    @Column(name = "sensorType") public int sensorType;
    @Column(name = "operatorType") public int operatorType;
    @Column(name = "value") public Integer value;
    @Column(name = "sensorId") public String sensorId;

    public SensorType getSensorType() {
        return SensorType.byId(sensorType);
    }

    public OperatorType getOperatorType() {
        return OperatorType.byId(operatorType);
    }
}
