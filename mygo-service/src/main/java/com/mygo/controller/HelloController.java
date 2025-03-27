package com.mygo.controller;

import com.mygo.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Tag(name = "测试接口")
public class HelloController {

    private final static Map<String, AtomicInteger> countmap = new HashMap<>();

    @Operation(summary = "测试接口")
    @GetMapping("/admin/hello")
    public Result<String> hello(HttpServletRequest request) {
        String ip = request.getRemoteAddr();
        AtomicInteger count = countmap.computeIfAbsent(ip, k -> new AtomicInteger(0));
        return Result.success(String.format("Hello world %d", count.incrementAndGet()));
    }
}
