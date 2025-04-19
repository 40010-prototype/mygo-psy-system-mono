package com.mygo.enumeration;

import com.fasterxml.jackson.annotation.JsonValue;

public enum ConsultStatus implements ValueEnum {
    PROGRESSING("progressing"),
    FINISHED("finished");

    private final String value;

    ConsultStatus(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String getValue() {
        return value;
    }
}
