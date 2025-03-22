package com.mygo.controller;

import com.mygo.domain.dto.LoginDTO;
import com.mygo.result.Result;
import com.mygo.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "登陆接口")
public class LoginController {

    @Autowired
    private LoginService loginservice;

    @PostMapping("/login")
    @Operation(summary = "登陆")
    public Result<String> login(@RequestBody LoginDTO loginDTO) {
        String token = loginservice.login(loginDTO);
        return Result.success(token);
    }
}
