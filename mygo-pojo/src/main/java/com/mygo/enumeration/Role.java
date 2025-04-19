package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Role implements ValueEnum {
    COUNSELOR("counselor"),
    SUPERVISOR("supervisor"),
    MANAGER("manager");

    private final String value;

    Role(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
