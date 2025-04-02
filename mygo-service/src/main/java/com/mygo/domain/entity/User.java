package com.mygo.domain.entity;

import lombok.Data;
@Data
public class User {
    private Long id;
    private String email;
    private String password;
    private String name;
    private String phone;
    private String gender;
    private Integer age;
    private String emergencyContact;
    private String emergencyContactPhone;

    public boolean needCompleteInfo() {
        return !(this.name != null && this.phone != null && this.gender != null && this.age != null &&
                this.emergencyContact != null && this.emergencyContactPhone != null);
    }
}
