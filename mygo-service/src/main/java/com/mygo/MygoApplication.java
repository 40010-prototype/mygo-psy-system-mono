package com.mygo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.mygo.mapper")
@SpringBootApplication
public class MygoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MygoApplication.class, args);
    }
}

