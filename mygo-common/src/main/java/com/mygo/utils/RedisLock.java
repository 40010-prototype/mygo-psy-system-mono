package com.mygo.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class RedisLock implements Lock {
    private String name;
    private StringRedisTemplate stringRedisTemplate;
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT=new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }
    public RedisLock(String name, StringRedisTemplate stringRedisTemplate) {
        this.name = name;
        this.stringRedisTemplate = stringRedisTemplate;
    }
    private static String KEY_PREFIX = "Lock:";
    @Override
    public boolean tryLock(long timeoutSeconds) {
        //获取线程id
        long threadId=Thread.currentThread().getId();
        //使用redis获取锁，设置过期时间
        Boolean success=stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX+name,threadId+"",timeoutSeconds, TimeUnit.SECONDS);
        //防止Boolean拆箱为boolean时出现空指针问题
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void unlock() {
        //释放锁
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(KEY_PREFIX+name),
                Thread.currentThread().getId()+""
        );
    }
}
