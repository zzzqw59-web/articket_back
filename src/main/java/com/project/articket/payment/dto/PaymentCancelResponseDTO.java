package com.project.articket.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.articket.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentCancelResponseDTO {
    @JsonProperty("orderId")
    private String paymentOrderId;

    @JsonProperty("totalAmount")
    private Long paymentAmount;

    @JsonProperty("status")
    private PaymentStatus paymentStatus;

    private List<CancelDTO> cancels;
}
