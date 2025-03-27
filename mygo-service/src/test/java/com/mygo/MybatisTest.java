package com.mygo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mygo.domain.entity.User;
import com.mygo.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(args = "--mpw.key=fqOS7bGCn3sxsTIL")
public class MybatisTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void selectId() {
        User user = userMapper.selectById(1L);
        System.out.println("user = " + user.getUsername());
    }

    @Test
    public void selectName() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, "千早爱音");
        User user = userMapper.selectOne(queryWrapper);
        System.out.println("user = " + user.getUsername() + user.getId() + user.getPassword());
    }

    @Test
    public void selectName2() {
        User user = userMapper.getUserByUsername("千早爱音");
        System.out.println("user = " + user.getUsername() + user.getId() + user.getPassword());
    }

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
