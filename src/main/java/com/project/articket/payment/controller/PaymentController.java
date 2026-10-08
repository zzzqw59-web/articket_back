package com.project.articket.payment.controller;

import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public PaymentConfirmResponseDTO confirmPayment(@Valid @RequestBody PaymentConfirmRequestDTO paymentConfirmRequestDTO) {
        return paymentService.confirmPayment(paymentConfirmRequestDTO);
    }
}
