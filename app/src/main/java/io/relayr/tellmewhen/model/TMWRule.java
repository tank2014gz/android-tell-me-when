package io.relayr.tellmewhen.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import io.relayr.tellmewhen.util.OperatorType;
import io.relayr.tellmewhen.util.SensorType;

@Table(name = "Rule")
public class TMWRule extends Model {

    @Column(name = "dbId") public String dbId;
    @Column(name = "dbRev") public String drRev;

    @Column(name = "isNotifying") public boolean isNotifying = true;
    @Column(name = "name") public String name;
    @Column(name = "transmitterId") public String transmitterId;
    @Column(name = "transmitterType") public String transmitterType;
    @Column(name = "transmitterName") public String transmitterName;
    @Column(name = "sensorType") public int sensorType;
    @Column(name = "operatorType") public int operatorType;
    @Column(name = "value") public Float value;
    @Column(name = "sensorId") public String sensorId;

    public SensorType getSensorType() {
        return SensorType.byId(sensorType);
    }

    public OperatorType getOperatorType() {
        return OperatorType.byId(operatorType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TMWRule)) return false;
        if (!super.equals(o)) return false;

        TMWRule tmwRule = (TMWRule) o;

        if (isNotifying != tmwRule.isNotifying) return false;
        if (operatorType != tmwRule.operatorType) return false;
        if (sensorType != tmwRule.sensorType) return false;
        if (dbId != null ? !dbId.equals(tmwRule.dbId) : tmwRule.dbId != null) return false;
        if (drRev != null ? !drRev.equals(tmwRule.drRev) : tmwRule.drRev != null) return false;
        if (name != null ? !name.equals(tmwRule.name) : tmwRule.name != null) return false;
        if (sensorId != null ? !sensorId.equals(tmwRule.sensorId) : tmwRule.sensorId != null)
            return false;
        if (transmitterId != null ? !transmitterId.equals(tmwRule.transmitterId) : tmwRule.transmitterId != null)
            return false;
        if (transmitterName != null ? !transmitterName.equals(tmwRule.transmitterName) : tmwRule.transmitterName != null)
            return false;
        if (transmitterType != null ? !transmitterType.equals(tmwRule.transmitterType) : tmwRule.transmitterType != null)
            return false;
        if (value != null ? !value.equals(tmwRule.value) : tmwRule.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = dbId != null ? dbId.hashCode() : 0;
        result = 31 * result + (drRev != null ? drRev.hashCode() : 0);
        result = 31 * result + (isNotifying ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (transmitterId != null ? transmitterId.hashCode() : 0);
        result = 31 * result + (transmitterType != null ? transmitterType.hashCode() : 0);
        result = 31 * result + (transmitterName != null ? transmitterName.hashCode() : 0);
        result = 31 * result + sensorType;
        result = 31 * result + operatorType;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + (sensorId != null ? sensorId.hashCode() : 0);
        return result;
    }
}
