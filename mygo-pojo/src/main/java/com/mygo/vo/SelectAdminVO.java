package com.mygo.vo;

import com.mygo.enumeration.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SelectAdminVO {
    private String id;
    private String username;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private Role role;
    private String status;
    private String profile;
    private String updatedAt;
    private String createdAt;
    private String lastLoginIp;
    private String lastLoginAt;
}
