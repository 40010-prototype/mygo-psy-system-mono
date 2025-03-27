package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.RedisConstant;
import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.User;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.UserMapper;
import com.mygo.service.LoginService;
import com.mygo.utils.JwtTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    UserMapper userMapper;

    @Autowired
    private JwtTool jwtTool;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名和密码登陆,如果顺利登陆,返回一个token
     * @param loginDTO 登陆DTO
     * @return token
     */
    @Override
    public String login(LoginDTO loginDTO) throws JsonProcessingException {
        //1.根据用户名查找是否存在该用户
        User user = userMapper.getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        //2.判断密码是否正确
        if (!user.getPassword()
                .equals(loginDTO.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        //3.生成JWT令牌
        String jwt = jwtTool.createJWT(user.getId());
        //4.将JWT保存在redis中
        String json = objectMapper.writeValueAsString(user);
        //这里不使用hash,因为要分别设置过期时间
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.JWT_KEY + ":" + user.getId(), json);
        //5.设置过期时间
        stringRedisTemplate.expire(RedisConstant.JWT_KEY + user.getId(), RedisConstant.JWT_EXPIRE,
                RedisConstant.JWT_EXPIRE_UNIT);
        //6.返回token
        return jwt;
    }

    @Override
    public User findByUserName(String username) {
        return userMapper.getUserByUsername(username);
    }

    @Override
    public void register(LoginDTO loginDTO) {
        userMapper.add(loginDTO.getUsername(),loginDTO.getPassword());
    }
}
