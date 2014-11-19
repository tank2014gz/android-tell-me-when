package io.relayr.tellmewhen.util;

public enum OperatorType {

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
}