package com.mygo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.AdminLoginDTO;
import com.mygo.dto.AdminRegisterDTO;
import com.mygo.dto.ResetPasswordDTO;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import com.mygo.vo.AdminInfoVO;
import com.mygo.vo.AdminLoginVO;
import com.mygo.vo.AdminSessionVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@Tag(name = "管理端接口")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    @Operation(summary = "登陆")
    public Result<AdminLoginVO> login(@RequestBody AdminLoginDTO adminLoginDTO) throws JsonProcessingException {
        AdminLoginVO adminLoginVO = adminService.login(adminLoginDTO);
        log.info("adminLoginVO.toString()");
        return Result.success(adminLoginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Void> register(@RequestBody AdminRegisterDTO adminRegisterDTO) throws JsonProcessingException {
        adminService.register(adminRegisterDTO);
        return Result.success();
    }

    @PostMapping("/send-email")
    @Operation(summary = "找回密码1：发送验证码")
    public Result<String> sendEmail(@RequestParam String name) {
        String email = adminService.sendEmail(name);
        return Result.success(email);
    }

    @PostMapping("/reset-password")
    @Operation(summary = "找回密码2：更改密码")
    public Result<String> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        adminService.resetPassword(resetPasswordDTO);
        return Result.success();
    }

    @GetMapping("/adminInfo")
    @Operation(summary = "获取用户信息")
    public Result<AdminInfoVO> adminInfo() {
        AdminInfoVO adminInfoVO = adminService.getAdminInfo();
        return Result.success(adminInfoVO);
    }

    @GetMapping("/sessions")
    @Operation(summary = "获取会话列表")
    public Result<List<AdminSessionVO>> getSession() {
        log.info("start get session");
        List<AdminSessionVO> session = adminService.getSession();
        return Result.success(session);
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<Void> getMessages(@PathVariable Integer sessionId, @RequestParam("limit") Integer limit,
                                    @RequestParam("offset") Integer offset) {
        return Result.success();
    }

}
