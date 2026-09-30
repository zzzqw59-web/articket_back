package com.project.articket.member.dto;

import lombok.Getter;

@Getter
public class PasswordResetRequestDTO {

    private String phone;
    private String newPassword;
}