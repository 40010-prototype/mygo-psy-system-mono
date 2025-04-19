package com.mygo.vo;

import com.mygo.enumeration.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

//Json序列化需要Getter
@Getter
@AllArgsConstructor
public class AdminLoginVO {

    private String token;

    private Role role;

}
