package com.capstone.BEApp.dto.auth;

import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String phone;
    private Integer age;
    private String gender;
    private String address;
}
