package com.mygo.utils;

import cn.hutool.core.exceptions.ValidateException;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTValidator;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.mygo.properties.JwtProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.util.Date;

/**
 * 自定义JWT工具,用于生成和解析token<br>
 * 为了不和hutool中的JwtUtil重名,改为JwtTool
 */
@Component
public class JwtTool {

    private final JWTSigner signer;

    @Autowired
    private JwtProperties jwtProperties;

    public JwtTool(KeyPair keyPair) {
        this.signer = JWTSignerUtil.createSigner("rs256", keyPair);
    }

    public String createJWT(Long id) {
        return JWT.create()
                .setPayload("id", id)
                .setExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.getTokenTTL()
                        .toMillis()))
                .setSigner(signer)
                .sign();
    }

    public Object parseJWT(String token) {
        //TODO 抛出异常
        //TODO 增加注释
        //1.判断token是不是空
        if (token == null) {
            return "未登录";
        }
        // 2.校验并解析jwt
        JWT jwt;
        try {
            jwt = JWT.of(token)
                    .setSigner(signer);
        } catch (Exception e) {
            return "无法解析token";
        }
        // 2.校验jwt是否有效
        if (!jwt.verify()) {
            // 验证失败
            return "无效的token";
        }
        // 3.校验是否过期
        try {
            JWTValidator.of(jwt)
                    .validateDate();
        } catch (ValidateException e) {
            return "token已经过期";
        }
        // 4.数据格式校验
        Object userPayload = jwt.getPayload("id");
        if (userPayload == null) {
            // 数据为空
            return "数据校验不通过";
        }
        // 5.数据解析
        try {
            return Long.valueOf(userPayload.toString());
        } catch (RuntimeException e) {
            // 数据格式有误
            return "格式有误";
        }
    }
}
