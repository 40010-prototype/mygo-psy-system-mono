package com.mygo.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.mygo.domain.enumeration.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private Role role;
}
