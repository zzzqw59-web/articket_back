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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    private void validatePageRequest(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPage() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 번호는 1 이상이어야 합니다."
            );
        }

        if (pageRequestDTO.getSize() < 1 || pageRequestDTO.getSize() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }
    }

    @PostMapping("/confirm")
    public PaymentConfirmResponseDTO confirmPayment(@Valid @RequestBody PaymentConfirmRequestDTO paymentConfirmRequestDTO, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return paymentService.confirmPayment(memberId, paymentConfirmRequestDTO);
    }

    // 내 결제 내역 목록 조회
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<PaymentListResponseDTO>> getMyPayments(
            @AuthenticationPrincipal Long memberId, // 로그인한 회원 ID 사용
            PageRequestDTO pageRequestDTO) {

        PageResponseDTO<PaymentListResponseDTO> response = paymentService.getMyPaymentList(memberId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 특정 결제 ID에 대한 결제 상세 조회
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDetailResponseDTO> getPaymentDetail(@PathVariable("paymentId") Long paymentId, Authentication authentication, PageRequestDTO pageRequestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        validatePageRequest(pageRequestDTO);
        PaymentDetailResponseDTO response = paymentService.getPaymentDetail(memberId, paymentId);
        return ResponseEntity.ok(response);
    }
}
