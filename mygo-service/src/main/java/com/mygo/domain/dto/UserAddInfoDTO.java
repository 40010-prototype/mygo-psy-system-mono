package com.mygo.domain.dto;

import lombok.Data;

@Data
public class UserAddInfoDTO {

    private String phone;

    private String gender;

    private Integer age;

    private String emergencyContact;

    private String emergencyContactPhone;

}