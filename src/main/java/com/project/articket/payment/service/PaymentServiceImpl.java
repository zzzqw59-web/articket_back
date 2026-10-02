package com.project.articket.payment.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;
import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.repository.PaymentRepository;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final WebClient webClient;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Override
    public PageResponseDTO<PaymentListResponseDTO> paymentList(String searchType, String keyword, PageRequestDTO pageRequestDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) authentication.getPrincipal();
        Page<Payment> paymentList;

        if ("paymentOrderId".equals(searchType)) {
            paymentList = paymentRepository.findByReservation_Member_MemberIdAndPaymentOrderIdContaining(memberId, keyword, pageRequestDTO.getPageable("paymentCreatedAt"));

        } else if ("exhibitionTitle".equals(searchType)) {
            paymentList = paymentRepository.findByReservation_Member_MemberIdAndReservation_Exhibition_ExhibitionTitleContaining(memberId, keyword, pageRequestDTO.getPageable("paymentCreatedAt"));
        } else {
            paymentList = paymentRepository.findByReservation_Member_MemberId(memberId, pageRequestDTO.getPageable("paymentCreatedAt"));
        }

        Page<PaymentListResponseDTO> dtoList = paymentList.map((payment) -> {
            PaymentListResponseDTO dto = new PaymentListResponseDTO();
            dto.setPaymentAmount(payment.getPaymentAmount());
            dto.setPaymentStatus(payment.getPaymentStatus());
            dto.setExhibitionTitle(payment.getReservation().getExhibition().getExhibitionTitle());
            dto.setPaymentCreatedAt(payment.getPaymentCreatedAt());
            dto.setPaymentId(payment.getPaymentId());
            dto.setPaymentOrderId(payment.getPaymentOrderId());
            return dto;
        });

        return new PageResponseDTO<>(dtoList.getContent(), pageRequestDTO, paymentList.getTotalElements());
    }

    @Override
    public PaymentDetailResponseDTO paymentDetail(Long paymentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) authentication.getPrincipal();
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("해당 결제가 존재하지 않습니다."));

        if (!memberId.equals(payment.getReservation().getMember().getMemberId())) {
            throw new IllegalArgumentException("동일한 회원이 아닙니다.");
        }

        PaymentDetailResponseDTO dto = new PaymentDetailResponseDTO();

        dto.setPaymentId(payment.getPaymentId());
        dto.setPaymentAmount(payment.getPaymentAmount());
        dto.setPaymentStatus(payment.getPaymentStatus());
        dto.setPaymentApprovedAt(payment.getPaymentApprovedAt());
        dto.setReservationId(payment.getReservation().getReservationId());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setPaymentCanceledAt(payment.getPaymentCanceledAt());
        dto.setPaymentCreatedAt(payment.getPaymentCreatedAt());
        dto.setPaymentRefundAmount(payment.getPaymentRefundAmount());
        dto.setPaymentOrderId(payment.getPaymentOrderId());

        return dto;
    }

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

    @Transactional
    @Override
    public void paymentRefund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new IllegalArgumentException("해당 결제가 존재하지 않습니다."));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long memberId = (Long) authentication.getPrincipal();

        if (!memberId.equals(payment.getReservation().getMember().getMemberId())) {
            throw new IllegalArgumentException("동일한 회원이 아닙니다.");
        }

        if (!"RESERVED".equals(payment.getReservation().getReservationStatus())) {
            throw new IllegalArgumentException("환불 할 수 없는 예약입니다.");
        }

        if (!"DONE".equals(payment.getPaymentStatus())) {
            throw new IllegalArgumentException("환불이 불가능합니다.");
        }

        LocalDate today = LocalDate.now();
        LocalDate reservationDay = payment.getReservation().getReservationDay();
        long daysUntil = ChronoUnit.DAYS.between(today, reservationDay);

        if (daysUntil <= 0) {
            throw new IllegalArgumentException("환불 할 수 없는 예약입니다.");
        }

        int refundRate;

        if (daysUntil >= 10) {
            refundRate = 100;
        } else if (daysUntil >= 7) {
            refundRate = 90;
        } else if (daysUntil >= 3) {
            refundRate = 80;
        } else {
            refundRate = 70;
        }

        long refundAmount = payment.getPaymentAmount() * refundRate / 100;
        payment.setPaymentRefundAmount(refundAmount);
    }
}
