package com.mygo.entity;

import com.mygo.enumeration.UserStatus;
import lombok.Data;

@Data
public class User {

    private Integer userId;

    private String email;

    private String password;

    private String name;

    private String phone;

    private String gender;

    private Integer age;

    private String emergencyContact;

    private String emergencyContactPhone;

    private String avatar;

    private UserStatus status;

}
