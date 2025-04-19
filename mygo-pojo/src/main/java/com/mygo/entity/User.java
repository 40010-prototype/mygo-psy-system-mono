package com.mygo.entity;

import com.mygo.enumeration.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class User {

    private Integer id;

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

    public boolean needCompleteInfo() {
        return !(this.name != null && this.phone != null && this.gender != null && this.age != null &&
                this.emergencyContact != null && this.emergencyContactPhone != null);
    }

}
