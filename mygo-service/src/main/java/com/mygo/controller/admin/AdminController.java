package com.mygo.controller.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.AdminLoginDTO;
import com.mygo.dto.AdminRegisterDTO;
import com.mygo.dto.ResetPasswordDTO;
import com.mygo.enumeration.Role;
import com.mygo.result.Result;
import com.mygo.service.AdminService;
import com.mygo.vo.*;
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
    public Result<List<AdminMessageVO>> getMessages(@PathVariable Integer sessionId,
                                                    @RequestParam("limit") Integer limit,
                                                    @RequestParam("offset") Integer offset) throws JsonProcessingException {
        List<AdminMessageVO> messages = adminService.getMessages(sessionId, limit, offset);
        return Result.success(messages);
    }

    @PatchMapping("/sessions/{sessionId}/read")
    public Result<Void> read(@PathVariable Integer sessionId) {
        log.info("ceshi1");
        adminService.read(sessionId);
        return Result.success();
    }

    @GetMapping("/users/counselors")
    public Result<List<SelectAdminVO>> getAllCounselor() {
        List<SelectAdminVO> addCounselor = adminService.getAllAdminByRole(Role.COUNSELOR);
        return Result.success(addCounselor);
    }

    @GetMapping("/users/supervisors")
    public Result<List<SelectAdminVO>> getAllSupervisor() {
        List<SelectAdminVO> addCounselor = adminService.getAllAdminByRole(Role.SUPERVISOR);
        return Result.success(addCounselor);
    }

    @GetMapping("/users")
    public Result<List<SelectUserVO>> getAllUser() {
        List<SelectUserVO> allUser = adminService.getAllUser();
        return Result.success(allUser);
    }

    @GetMapping("/users/{userId}")
    public Result<SelectUserVO> getAllUserById(@PathVariable Integer userId) {
        SelectUserVO user = adminService.getAllUserById(userId);
        return Result.success(user);
    }

    @GetMapping("/users/admin/{adminId}")
    public Result<SelectAdminVO> getAdminById(@PathVariable Integer adminId) {
        SelectAdminVO admin = adminService.getAdminById(adminId);
        return Result.success(admin);
    }

    @GetMapping("/getBindSupervisorInfo")
    public Result<HelpVO> getHelpSessionId(){
        HelpVO helpSessionId = adminService.getHelpSessionId();
        return Result.success(helpSessionId);
    }

    @PostMapping("/assignments/{supervisorId}/{counselorId}")
    public Result<Void> setHelp(@PathVariable Integer counselorId, @PathVariable Integer supervisorId, 
                               @RequestParam(defaultValue = "bind") String action) {
        if ("bind".equals(action)) {
            adminService.setHelp(supervisorId, counselorId);
        } else if ("unbind".equals(action)) {
            adminService.removeHelp(supervisorId, counselorId);
        } else {
            return Result.error("不支持的操作");
        }
        return Result.success();
    }

}
