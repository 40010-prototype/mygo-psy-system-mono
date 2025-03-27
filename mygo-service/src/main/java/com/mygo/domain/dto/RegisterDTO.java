package com.mygo.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
@Tag(name = "注册DTO", description = "注册DTO")
public class RegisterDTO {

    @Schema(description = "用户名")
    String name;

    @Schema(description = "密码")
    String password;

    @Pattern(regexp = "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", message = "请输入有效的邮箱地址")
    @Schema(description = "邮箱")
    String email;
}
