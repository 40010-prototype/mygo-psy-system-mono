package com.mygo.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.mygo.domain.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;

@Data
@Tag(name = "用户实体", description = "用户实体")
public class Admin {

    @Schema(description = "用户ID")
    private Integer adminId;

    @Schema(description = "用户名")
    private String name;

    @Schema(description = "用户密码")
    private String password;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "用户角色")
    private Role role;
}
