package com.mygo.controller.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mygo.dto.*;
import com.mygo.result.Result;
import com.mygo.service.UserService;
import com.mygo.vo.ActiveCounselorVO;
import com.mygo.vo.AdminMessageVO;
import com.mygo.vo.UserLoginVO;
import com.mygo.vo.UserMessageVO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Tag(name = "用户端接口")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) {
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    @PostMapping("/register")
    @Operation(summary = "注册")
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.register(userRegisterDTO);
        return Result.success();
    }

    @PostMapping("/addInfo")
    @Operation(summary = "添加信息")
    public Result<Void> addInfo(@RequestBody UserAddInfoDTO userAddInfoDTO) {
        userService.addInfo(userAddInfoDTO);
        return Result.success();
    }

    @GetMapping("/getActiveCounselor")
    public Result<List<ActiveCounselorVO>> getActiveCounselor() {
        List<ActiveCounselorVO> activeCounselor = userService.getActiveCounselor();
        return Result.success(activeCounselor);
    }

    @PostMapping("/setSession/{counselorId}")
    public Result<Void> setSession(@PathVariable("counselorId") Integer counselorId) throws JsonProcessingException {
        userService.setSession(counselorId);
        return Result.success();
    }

    @PostMapping("/endSession")
    public Result<Void> endSession(@RequestBody EndSessionDTO endSessionDTO) {
        userService.endSession(endSessionDTO);
        return Result.success();
    }

    @GetMapping("/sessions/messages")
    public Result<List<UserMessageVO>> getMessages(@RequestParam Integer counselorId) throws JsonProcessingException {
        List<UserMessageVO> messages = userService.getMessages(counselorId);
        return Result.success(messages);
    }

}
