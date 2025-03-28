package com.mygo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.domain.dto.LoginDTO;
import com.mygo.domain.dto.RegisterDTO;
import com.mygo.domain.vo.LoginVO;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "用户接口")
public class AdminController {

    private final AdminService adminservice;

    @Autowired
    public AdminController(AdminService adminservice) {
        this.adminservice = adminservice;
    }

    @PostMapping("/login")
    @Operation(summary = "登陆")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) throws JsonProcessingException {
        LoginVO loginVO = adminservice.login(loginDTO);
        return Result.success(loginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Void> register(@RequestBody @Valid RegisterDTO registerDTO) {
        adminservice.register(registerDTO);
        return Result.success();
    }
}
