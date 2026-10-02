package com.project.articket.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmRequestDTO {
    @NotBlank(message = "결제 키는 필수 입력값입니다.")
    private String paymentKey;

    @NotBlank(message = "주문 번호는 필수 입력값입니다.")
    private String orderId;

    @NotNull(message = "결제 금액은 필수 입력값입니다.")
    @Positive(message = "결제 금액은 0보다 커야 합니다.")
    private Long amount;
}
