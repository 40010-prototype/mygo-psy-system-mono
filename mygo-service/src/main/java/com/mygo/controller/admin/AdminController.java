package com.mygo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.domain.dto.AdminLoginDTO;
import com.mygo.domain.dto.AdminRegisterDTO;
import com.mygo.domain.dto.ResetPasswordDTO;
import com.mygo.domain.dto.UserDTO;
import com.mygo.domain.vo.AdminLoginVO;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) throws JsonProcessingException {
        AdminLoginVO adminLoginVO = adminservice.login(adminLoginDTO);
        return Result.success(adminLoginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Void> register(@RequestBody @Valid AdminRegisterDTO adminRegisterDTO) {
        adminservice.register(adminRegisterDTO);
        return Result.success();
    }

    @PostMapping("/send-email")
    @Operation(summary = "忘记密码1:发送验证码")
    public Result<String> sendEmail(@RequestParam String name) {
        String email = adminservice.sendEmail(name);
        return Result.success(email);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "忘记密码2:更改密码")
    public Result<String> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        adminservice.resetPassword(resetPasswordDTO);
        return Result.success();
    }

    @GetMapping("/userInfo")
    @Operation(summary = "获取用户信息")
    public Result<UserDTO> userInfo() {
        return adminservice.getUserInfo();
    }
}
