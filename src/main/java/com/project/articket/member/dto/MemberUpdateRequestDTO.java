package com.project.articket.member.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberUpdateRequestDTO {

    private String nickname;
    private String password;
    private String phone;
}