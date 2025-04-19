package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum Sender implements ValueEnum {
    Admin("admin"),
    User("user");

    private final String value;

    Sender(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
