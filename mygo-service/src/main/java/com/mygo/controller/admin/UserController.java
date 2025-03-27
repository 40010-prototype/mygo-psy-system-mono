package com.mygo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.entity.User;
import com.mygo.result.Result;
import com.mygo.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "用户接口")
public class UserController {

    @Autowired
    private LoginService loginservice;

    @PostMapping("/login")
    @Operation(summary = "登陆")
    public Result<String> login(@RequestBody LoginDTO loginDTO) throws JsonProcessingException {
        String token = loginservice.login(loginDTO);
        return Result.success(token);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result register(@RequestBody LoginDTO loginDTO) {
        //查询
        User user = loginservice.findByUserName(loginDTO.getUsername());
        if(user == null) {
            loginservice.register(loginDTO);
            return Result.success();
        }
        else{
            return Result.error("用户名已被占用");
        }
        //注册

    }
}
