package com.project.articket.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequestDTO {

    private String email;

    private String password;

    private String nickname;

    private String name;

    private String phone;
}