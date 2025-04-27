package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ScheduleStatus implements ValueEnum {
    APPROVED("approved"),
    PENDING("pending");

    private final String value;

    ScheduleStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
