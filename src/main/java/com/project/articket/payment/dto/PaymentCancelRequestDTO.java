package com.project.articket.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelRequestDTO {
    @NotBlank(message = "환불 사유는 필수입니다.")
    private String cancelReason;

    private Long cancelAmount;
}
