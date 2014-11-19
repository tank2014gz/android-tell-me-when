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
}