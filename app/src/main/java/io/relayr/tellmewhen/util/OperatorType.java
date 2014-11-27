package io.relayr.tellmewhen.util;

import java.io.Serializable;

public enum OperatorType implements Serializable{

    EQUALS("="), GREATER(">"), LESS("<");

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
}