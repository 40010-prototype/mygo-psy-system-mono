package com.mygo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.entity.Admin;
import com.mygo.entity.User;
import com.mygo.enumeration.MessageType;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

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
        adminMapper.getCounselorStatusById(396);
    }

    @Test
    void SqlExceptionTest6() throws JsonProcessingException {
        LocalDate currentDate = LocalDate.now();  // 当前日期：YYYY-MM-DD
        System.out.println(new Date());
        // 将 LocalDate 转换为 java.util.Date，且时间为 00:00:00
        Date date = Date.from(currentDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        System.out.println(date);
// 获取当前时间（精确到 HH:mm:ss，去除毫秒）
        LocalTime currentTime = LocalTime.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);  // 去除毫秒部分

// 转换成 java.sql.Date

    }

}
