package io.relayr.tellmewhen.consts;

import java.io.Serializable;

public enum OperatorType implements Serializable{

    GREATER(">"), LESS("<");

    private final String value;

    OperatorType(String name) {
        this.value = name;
    }

    public String getValue() {
        return value;
    }

    public static OperatorType byId(int id) {
        for (OperatorType type : values()) {
            if(type.ordinal() == id)
                return type;
        }
        return null;
    }

    public static OperatorType getByValue(String value) {
        if(value == null)
            return null;

        for (OperatorType type : values()) {
            if (type.getValue().equals(value.toLowerCase())) return type;
        }

        return null;
    }
}