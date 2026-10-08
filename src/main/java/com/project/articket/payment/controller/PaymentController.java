package com.project.articket.payment.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;
import com.project.articket.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public PaymentConfirmResponseDTO confirmPayment(@Valid @RequestBody PaymentConfirmRequestDTO paymentConfirmRequestDTO) {
        return paymentService.confirmPayment(paymentConfirmRequestDTO);
    }

    // =========================================================
    // =========================================================


    // 내 결제 내역 목록 조회
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<PaymentListResponseDTO>> getMyPayments(
            @AuthenticationPrincipal Long memberId, // 로그인한 회원 ID 사용
            PageRequestDTO pageRequestDTO) {

        PageResponseDTO<PaymentListResponseDTO> response = paymentService.getMyPaymentList(memberId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 특정 예약 ID에 대한 결제 상세 조회
    @GetMapping("/reservation/{reservationId}")
    public ResponseEntity<PaymentDetailResponseDTO> getPaymentDetail(@PathVariable("reservationId") Long reservationId) {
        PaymentDetailResponseDTO response = paymentService.getPaymentDetail(reservationId);
        return ResponseEntity.ok(response);
    }
}
