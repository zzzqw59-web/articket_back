package com.project.articket.payment.service;

import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;

public interface PaymentService {
    PaymentConfirmResponseDTO paymentConfirm(PaymentConfirmRequestDTO paymentConfirmRequestDTO);
}
