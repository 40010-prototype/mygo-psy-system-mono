package com.mygo.dto;

import lombok.Data;

@Data
public class ResetPasswordDTO {

    private String name;

    private String verifyCode;

    private String password;

}
