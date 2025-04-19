package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum MessageStatus implements ValueEnum {

    SENDING("sending"),
    SENT("sent"),
    DELIVERED("delivered"),
    READ("read"),
    FAILED("failed");

    private final String value;

    MessageStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
