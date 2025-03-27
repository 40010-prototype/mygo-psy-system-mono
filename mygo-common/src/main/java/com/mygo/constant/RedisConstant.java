package com.mygo.constant;

import java.util.concurrent.TimeUnit;

public class RedisConstant {

    public static final String JWT_KEY = "user:login:token:";

    public static final int JWT_EXPIRE = 12;

    public static final TimeUnit JWT_EXPIRE_UNIT = TimeUnit.HOURS;
}
