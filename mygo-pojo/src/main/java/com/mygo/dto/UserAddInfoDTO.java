package com.mygo.dto;

import lombok.Data;

@Data
public class UserAddInfoDTO {

    private String name;

    private String phone;

    private String gender;

    private Integer age;

    private String emergencyContact;

    private String emergencyContactPhone;

}