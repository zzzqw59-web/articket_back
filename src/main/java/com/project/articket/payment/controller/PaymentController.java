package com.project.articket.payment.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;
import com.project.articket.payment.service.PaymentService;
import com.project.articket.reservation.dto.ReservationCancelDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/me")
    public PageResponseDTO<PaymentListResponseDTO> paymentList(@RequestParam(required = false) String searchType, @RequestParam(required = false) String keyword, PageRequestDTO pageRequestDTO) {
        return paymentService.paymentList(searchType, keyword, pageRequestDTO);
    }

    @GetMapping("/{paymentId}")
    public PaymentDetailResponseDTO paymentDetail(@PathVariable Long paymentId) {

        PaymentDetailResponseDTO paymentDetailResponseDTO = paymentService.paymentDetail(paymentId);
        return paymentDetailResponseDTO;
    }

    @PostMapping("/confirm")
    PaymentConfirmResponseDTO paymentApprove(@Valid @RequestBody PaymentConfirmRequestDTO paymentConfirmRequestDTO) {
        PaymentConfirmResponseDTO paymentConfirmResponseDTO = paymentService.paymentConfirm(paymentConfirmRequestDTO);
        return paymentConfirmResponseDTO;
    }

    @DeleteMapping("/{paymentId}/cancel")
    void paymentRefund(@PathVariable Long paymentId, @Valid @RequestBody ReservationCancelDTO reservationCancelDTO) {
        paymentService.paymentRefund(paymentId, reservationCancelDTO);
    }
}
