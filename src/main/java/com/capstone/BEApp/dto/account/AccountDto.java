package com.capstone.BEApp.dto.account;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountDto {
    private Long id;
    private String name;
    private String avatar;
    private String email;
    private String phone;
    private Integer age;
    private String gender;
    private String address;
    private LocalDateTime createdDate;

    private String role;
}
