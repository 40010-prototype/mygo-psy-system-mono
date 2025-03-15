package com.mygo.controller;


import com.mygo.domain.dto.LoginDTO;
import com.mygo.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class LoginController {
    @Autowired
    private LoginService loginservice;

    @PostMapping("/login")
    public String login(@RequestBody LoginDTO loginDTO){
        System.out.println(loginDTO.getPassword());
        return loginservice.login(loginDTO);
    }
}
