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
public class PaymentDetailResponseDTO {
    private Long paymentId;
    private String paymentOrderId;
    private Long reservationId;
    private Long paymentAmount;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentApprovedAt;
    private String paymentMethod;
    private Long paymentRefundAmount;
    private LocalDateTime paymentCreatedAt;
    private LocalDateTime paymentCanceledAt;
}
