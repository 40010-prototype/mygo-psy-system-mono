package com.mygo.enumeration;

public interface ValueEnum {

    static <T extends Enum<T> & ValueEnum> T fromValue(String value, Class<T> enumClass) {
        // 获取枚举常量
        for (T enumConstant : enumClass.getEnumConstants()) {
            if (enumConstant.getValue()
                    .equals(value)) {
                return enumConstant;
            }
        }
        throw new IllegalArgumentException("Unknown value: " + value);
    }

    String getValue();

}
