package com.mygo.dto;

import com.mygo.enumeration.Role;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String name;

    private String email;

    private String phone;

    private Role role;

}
