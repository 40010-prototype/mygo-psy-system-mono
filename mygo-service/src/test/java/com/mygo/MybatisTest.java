package com.mygo;

import cn.hutool.core.util.IdUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.domain.entity.Message;
import com.mygo.mapper.AdminMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,args = "--mpw.key=fqOS7bGCn3sxsTIL")

public class MybatisTest {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void SqlExceptionTest() {
        adminMapper.addAdmin(IdUtil.getSnowflakeNextId(), "五河琴里", "123", "kk@qq.com");
        adminMapper.addAdmin(IdUtil.getSnowflakeNextId(), "五河琴里", "123", "kk@qq.com");
    }

    @Test
    void SqlExceptionTest2() {
        System.out.println(Integer.MAX_VALUE + 1);
    }

    @Test
    void SqlExceptionTest3() throws JsonProcessingException {
        Message message = new Message();
        message.setFromId(1L);
        message.setToId(2L);
        message.setMessage("test");
        String ms="{\"fromId\":1,\"toId\":2,\"message\":\"test\"}";
        Message ms2=objectMapper.readValue(ms, Message.class);
        System.out.println(objectMapper.valueToTree(message));
    }
}
