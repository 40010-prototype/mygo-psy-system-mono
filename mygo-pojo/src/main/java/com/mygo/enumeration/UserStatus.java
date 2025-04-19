package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserStatus implements ValueEnum {
    ACTIVE("active"),
    UNACTIVE("unactive");

    private final String value;

    UserStatus(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String getValue() {
        return value;
    }
}
