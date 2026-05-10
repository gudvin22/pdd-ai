package com.pdd.pddai.dto;

import lombok.Data;

@Data
public class UserRegistrationDto {
    private String telegramId;
    private String email;
    private String password;
    private String userName;
}
