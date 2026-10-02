package com.project.articket.payment.dto;

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
    @NotNull(message = "결제 키는 필수 입력 값입니다.")
    private Long paymentId;

//    @NotNull(message = "예약 번호는 필수 입력 값입니다.")

}
