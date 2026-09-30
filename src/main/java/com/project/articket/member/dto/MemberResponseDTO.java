package com.project.articket.member.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MemberResponseDTO {

    private String email;
    private String nickname;
    private String name;
    private String phone;
    private String memberType;
    private Integer memberStatus;
    private LocalDateTime joinCreatedAt;
}