package com.mygo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.RedisConstant;
import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.Admin;
import com.mygo.domain.vo.LoginVO;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.AdminMapper;
import com.mygo.service.AdminService;
import com.mygo.utils.JwtTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    AdminMapper adminMapper;

    @Autowired
    private JwtTool jwtTool;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 根据用户名和密码登陆,如果顺利登陆,返回一个token
     *
     * @param loginDTO 登陆DTO
     * @return token
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) throws JsonProcessingException {
        //1.根据用户名查找是否存在该用户
        Admin admin = adminMapper.getAdminByName(loginDTO.getName());
        if (admin == null) {
            throw new BadRequestException("用户不存在");
        }
        //2.判断密码是否正确
        if (!admin.getPassword()
                .equals(loginDTO.getPassword())) {
            throw new BadRequestException("密码错误");
        }
        //3.生成JWT令牌
        String jwt = jwtTool.createJWT(admin.getId());
        //4.将JWT保存在redis中
        String json = objectMapper.writeValueAsString(admin);
        //这里不使用hash,因为要分别设置过期时间
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.JWT_KEY  + admin.getId(), json);
        //5.设置过期时间
        stringRedisTemplate.expire(RedisConstant.JWT_KEY + admin.getId(), RedisConstant.JWT_EXPIRE,
                RedisConstant.JWT_EXPIRE_UNIT);
        //6.返回token
        return new LoginVO(jwt, admin.getRole());
    }

    @Override
    public Admin findByUserName(String username) {
        return adminMapper.getAdminByName(username);
    }

    @Override
    public void register(LoginDTO loginDTO) {
        adminMapper.add(loginDTO.getName(),loginDTO.getPassword());
    }
}
