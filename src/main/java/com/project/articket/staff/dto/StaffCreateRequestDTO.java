package com.project.articket.staff.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class StaffCreateRequestDTO {

    private String email;

    private String password;

    private String nickname;

    private String name;

    private String phone;
}