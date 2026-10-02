package com.project.articket.payment.dto;

import com.project.articket.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentConfirmResponseDTO {
    private Long paymentId;
    private String paymentOrderId;
    private Long paymentAmount;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private LocalDateTime paymentApprovedAt;
}
