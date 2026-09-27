package com.project.articket.verification.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class VerificationSendRequestDTO {

    private String phone;
    private String type;
}