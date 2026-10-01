package com.project.articket.payment.service;

import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.repository.PaymentRepository;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final WebClient webClient;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Transactional
    @Override
    public PaymentConfirmResponseDTO paymentConfirm(PaymentConfirmRequestDTO paymentConfirmRequestDTO) {
        Reservation reservation = reservationRepository.findByReservationOrderId(paymentConfirmRequestDTO.getOrderId()).orElseThrow(() -> new IllegalArgumentException("해당 예약이 존재하지 않습니다."));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Long memberId = (Long) authentication.getPrincipal();

        if (!memberId.equals(reservation.getMember().getMemberId())) {
            throw new IllegalArgumentException("동일한 회원이 아닙니다.");
        }
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException("결제할 수 없는 예약입니다.");
        }

        if (!reservation.getReservationAmount().equals(paymentConfirmRequestDTO.getAmount())) {
            throw new IllegalArgumentException("결제 금액이 올바르지 않습니다.");
        }

        String auth = tossSecretKey + ":";

        PaymentConfirmResponseDTO response = webClient.post()
                .uri("https://api.tosspayments.com/v1/payments/confirm")
                .header("Authorization", "Basic " + Base64.getEncoder().encodeToString(auth.getBytes()))
                .bodyValue(paymentConfirmRequestDTO)
                .retrieve()
                .onStatus(status -> status.isError(), clientResponse -> clientResponse.bodyToMono(String.class).map(message -> new IllegalArgumentException("Toss 결제 승인 실패: " + message)))
                .bodyToMono(PaymentConfirmResponseDTO.class)
                .block();

        Payment payment = new Payment(reservation, response.getPaymentOrderId(), response.getPaymentKey(), response.getPaymentAmount(), response.getPaymentStatus(), response.getPaymentMethod(), response.getPaymentApprovedAt());
        reservation.setReservationStatus(ReservationStatus.RESERVED);
        paymentRepository.save(payment);

        return response;
    }
}
