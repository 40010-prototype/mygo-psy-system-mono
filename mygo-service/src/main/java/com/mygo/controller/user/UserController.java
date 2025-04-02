package com.mygo.controller.user;

import com.mygo.domain.dto.UserLoginDTO;
import com.mygo.domain.dto.UserRegisterDTO;
import com.mygo.result.Result;
import com.mygo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Result<Void> login(@RequestBody UserLoginDTO userLoginDTO) {
        System.out.println(userLoginDTO.getEmail() + " " + userLoginDTO.getPassword());
        return Result.success();
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success();
    }
}
