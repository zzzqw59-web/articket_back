package com.project.articket.payment.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;
import com.project.articket.reservation.dto.ReservationCancelDTO;

public interface PaymentService {
    PaymentConfirmResponseDTO paymentConfirm(PaymentConfirmRequestDTO paymentConfirmRequestDTO);

    PaymentDetailResponseDTO paymentDetail(Long paymentId);

    PageResponseDTO<PaymentListResponseDTO> paymentList(String searchType, String keyword, PageRequestDTO pageRequestDTO);

    void paymentRefund(Long paymentId, ReservationCancelDTO reservationCancelDTO);
}
