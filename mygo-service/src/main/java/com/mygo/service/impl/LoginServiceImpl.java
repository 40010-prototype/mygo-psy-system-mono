package com.mygo.service.impl;

import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.User;
import com.mygo.exception.BadRequestException;
import com.mygo.mapper.UserMapper;
import com.mygo.service.LoginService;
import com.mygo.utils.JwtTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    UserMapper userMapper;

    @Autowired
    private JwtTool jwtTool;

    @Override
    public String login(LoginDTO loginDTO) {
        //1.根据用户名查找是否存在改用户
        User user = userMapper.getUserByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        //2.判断密码是否正确
        if (!user.getPassword()
                .equals(loginDTO.getPassword())) {
            return "密码不正确";
        }
        //3.返回JWT令牌
        return jwtTool.createJWT(user.getId());
    }
}
