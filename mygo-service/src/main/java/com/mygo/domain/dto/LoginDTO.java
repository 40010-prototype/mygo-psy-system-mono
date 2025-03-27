package com.mygo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Data
@Tag(name = "登陆DTO", description = "登陆DTO")
public class LoginDTO {

    @Schema(description = "用户名")
    String username;

    @Schema(description = "密码")
    String password;
}
