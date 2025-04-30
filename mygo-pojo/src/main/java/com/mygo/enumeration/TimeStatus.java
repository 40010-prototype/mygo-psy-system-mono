package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum TimeStatus implements ValueEnum {
    AVAILABLE("available"),
    BUSY("busy"),
    UNAVAILABLE("unavailable");

    private final String value;

    TimeStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
