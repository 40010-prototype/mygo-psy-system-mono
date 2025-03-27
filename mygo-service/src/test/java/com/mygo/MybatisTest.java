package com.mygo;

import com.mygo.mapper.AdminMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(args = "--mpw.key=fqOS7bGCn3sxsTIL")
public class MybatisTest {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Test
    void testString() {
        // 写入一条String数据
        stringRedisTemplate.opsForValue()
                .set("name", "虎哥");
        // 获取string数据
        Object name = stringRedisTemplate.opsForValue()
                .get("name");
        System.out.println("name = " + name);
    }
}
