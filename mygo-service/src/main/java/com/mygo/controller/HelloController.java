package com.mygo.controller;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
public class HelloController {

    private final static Map<String, AtomicInteger> countmap = new HashMap<>();

    @GetMapping("/hello")
    public String hello(HttpServletRequest request){
        String ip = request.getRemoteAddr();
        AtomicInteger count = countmap.get(ip);
        if (count == null) {
            count = new AtomicInteger(0);
            countmap.put(ip, count);
        }
        return String.format("Hello world %d",count.incrementAndGet());
    }
}
