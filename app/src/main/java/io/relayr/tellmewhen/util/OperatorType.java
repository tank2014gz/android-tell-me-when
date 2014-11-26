package io.relayr.tellmewhen.util;

import java.io.Serializable;
import java.lang.reflect.TypeVariable;

public enum OperatorType implements Serializable{

    EQUALS("="), GREATER(">"), LESS("<");

    private final String name;

    OperatorType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static OperatorType getByName(String type) {
        for (OperatorType t : values()) {
            if (t.getName().equals(type)) return t;
        }

        return null;
    }

    public static OperatorType byId(int id) {
        for (OperatorType type : values()) {
            if(type.ordinal() == id)
                return type;
        }
        return null;
    }
}