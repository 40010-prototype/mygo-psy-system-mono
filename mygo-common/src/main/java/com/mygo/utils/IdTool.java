package com.mygo.utils;

import com.mygo.constant.RedisConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class IdTool {

    private final StringRedisTemplate stringRedisTemplate;

    @Autowired
    public IdTool(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public int getPersonId() {
        String id = stringRedisTemplate.opsForValue()
                .get(RedisConstant.PERSON_INDEX_KEY);
        if (id == null) {
            stringRedisTemplate.opsForValue()
                    .set(RedisConstant.PERSON_INDEX_KEY, "0");
            return 0;
        }
        int personId = Integer.parseInt(id);
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.PERSON_INDEX_KEY, String.valueOf(personId + 1));
        return personId;
    }
}
