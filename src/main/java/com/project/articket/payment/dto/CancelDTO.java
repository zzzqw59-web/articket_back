package com.project.articket.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CancelDTO {
    private Long cancelAmount;
    private String cancelReason;
    private LocalDateTime canceledAt;
    private String cancelStatus;
}
