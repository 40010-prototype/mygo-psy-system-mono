package com.mygo.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminUpdateDTO {
    private String id;
    private String name;
    private String email;
    private String phone;
    private ProfileDTO profile;
    
    @Data
    public static class ProfileDTO {
        private String title;
        private String qualification;
        private List<String> expertise;
        private String introduction;
        private String supervisorId;
        private String supervisorName;
    }
} 