package com.mygo.domain.vo;

import com.mygo.domain.enumeration.Role;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class AdminLoginVO {
    private String token;
    private Role role;
}
