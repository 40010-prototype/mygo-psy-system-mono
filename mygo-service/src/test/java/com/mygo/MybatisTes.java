package com.mygo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.entity.Admin;
import com.mygo.entity.User;
import com.mygo.enumeration.UserStatus;
import com.mygo.mapper.AdminMapper;
import com.mygo.mapper.ChatMapper;
import com.mygo.mapper.UserMapper;
import com.mygo.utils.IdTool;
import com.mygo.utils.JwtTool;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, args = "--mpw.key=fqOS7bGCn3sxsTIL")
public class MybatisTes {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTool jwtTool;

    @Autowired
    private IdTool idTool;

    @Autowired
    private ChatMapper chatMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    void SqlExceptionTest() {
        Admin admin = adminMapper.getAdminByName("古怪之裤子");
    }

    @Test
    void SqlExceptionTest2() {
        System.out.println(Integer.MAX_VALUE + 1);
    }

    @Test
    void SqlExceptionTest4() throws JsonProcessingException {
        System.out.println(jwtTool.createJWT(5));
    }

    @Test
    void SqlExceptionTest5() throws JsonProcessingException {
        User user = userMapper.selectUserById(1);
        System.out.println(user.getStatus());
    }

    @Test
    void SqlExceptionTest6() throws JsonProcessingException {
        UserStatus userStatus = chatMapper.getUserStatus(340);
        System.out.println(userStatus);

    }

}
