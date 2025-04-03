package com.mygo.constant;

import java.util.concurrent.TimeUnit;

public class RedisConstant {

    //TODO 改名

    //admin:login:token:{id)
    public static final String ADMIN_JWT_KEY = "admin:login:token:";

    public static final int JWT_EXPIRE = 12;

    public static final TimeUnit JWT_EXPIRE_UNIT = TimeUnit.HOURS;

    //admin:reset:verify:{name}
    public static final String VERIFY_KEY = "admin:reset:verify:";

    public static final int VERIFY_EXPIRE = 10;

    public static final TimeUnit VERIFY_EXPIRE_UNIT = TimeUnit.MINUTES;

    public static final String USER_JWT_KEY = "user:login:token:";

    public static final String PERSON_INDEX_KEY = "index:person";

    public static final String CHAT_INDEX_KEY = "index:chat";

}
