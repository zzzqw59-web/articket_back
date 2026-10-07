package com.project.articket.withdraw.dto;

import com.project.articket.withdraw.enums.WithdrawStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WithdrawResponseDTO {

    private WithdrawStatus withdrawStatus;

    private LocalDateTime withdrawCreatedAt;

    private LocalDateTime withdrawCanceledAt;

    private LocalDateTime withdrawnAt;

    private LocalDateTime withdrawDue;
}