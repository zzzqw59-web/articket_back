package com.project.articket.payment.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;

public interface PaymentService {
    PaymentConfirmResponseDTO confirmPayment(PaymentConfirmRequestDTO paymentConfirmRequestDTO);

    void cancelPayment(Long reservationId, Long refundAmount);

    PageResponseDTO<PaymentListResponseDTO> getMyPaymentList(Long memberId, PageRequestDTO pageRequestDTO);

    PaymentDetailResponseDTO getPaymentDetail(Long reservationId);
}
