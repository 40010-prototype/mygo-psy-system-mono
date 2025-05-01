package com.mygo.vo;

import com.mygo.enumeration.UserStatus;
import lombok.Data;

@Data
public class SelectUserVO {
    private String id;

    private String email;

    private String name;

    private String phone;

    private String gender;

    private Integer age;

    private String avatar;

    private UserStatus status;
}
