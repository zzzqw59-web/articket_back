package com.project.articket.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String paymentKey;
    private Long reservationId;

    @JsonProperty("orderId")
    private String paymentOrderId;

    @JsonProperty("totalAmount")
    private Long paymentAmount;

    @JsonProperty("status")
    private PaymentStatus paymentStatus;

    @JsonProperty("method")
    private String paymentMethod;

    @JsonProperty("approvedAt")
    private LocalDateTime paymentApprovedAt;
}
