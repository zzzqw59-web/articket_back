package com.project.articket.payment.service;

import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.entity.PaymentStatus;
import com.project.articket.payment.repository.PaymentRepository;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final WebClient paymentWebClient;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    @Transactional
    @Override
    public PaymentConfirmResponseDTO confirmPayment(
            PaymentConfirmRequestDTO paymentConfirmRequestDTO
    ) {

        // 1. 주문 번호로 예약 조회
        Reservation reservation = reservationRepository
                .findByReservationOrderId(
                        paymentConfirmRequestDTO.getOrderId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약 정보를 찾을 수 없습니다."
                        )
                );

        // 2. 예약 상태 확인
        if (reservation.getReservationStatus()
                != ReservationStatus.PENDING) {

            throw new IllegalArgumentException(
                    "결제할 수 없는 예약입니다."
            );
        }

        // 3. 결제 금액 검증
        if (!reservation.getReservationAmount()
                .equals(paymentConfirmRequestDTO.getAmount())) {

            throw new IllegalArgumentException(
                    "결제 금액이 올바르지 않습니다."
            );
        }

        // 4. Toss Secret Key를 Base64로 인코딩
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString(
                        (tossSecretKey + ":")
                                .getBytes(StandardCharsets.UTF_8)
                );

        // 5. Toss 결제 승인 요청
        Map<String, Object> tossResponse = paymentWebClient.post()
                .uri("/v1/payments/confirm")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + encodedSecretKey
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "paymentKey",
                        paymentConfirmRequestDTO.getPaymentKey(),
                        "orderId",
                        paymentConfirmRequestDTO.getOrderId(),
                        "amount",
                        paymentConfirmRequestDTO.getAmount()
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        // 6. Toss 승인 결과에서 필요한 값 추출
        String paymentKey =
                (String) tossResponse.get("paymentKey");

        String orderId =
                (String) tossResponse.get("orderId");

        Number totalAmount =
                (Number) tossResponse.get("totalAmount");

        String method =
                (String) tossResponse.get("method");

        String approvedAt =
                (String) tossResponse.get("approvedAt");

        // 7. Payment 생성
        LocalDateTime approvedAtTime = null;

        if (approvedAt != null) {
            approvedAtTime = LocalDateTime.parse(
                    approvedAt.substring(0, 19)
            );
        }

        Payment payment = new Payment(
                reservation,
                orderId,
                paymentKey,
                totalAmount.longValue(),
                PaymentStatus.DONE,
                method,
                approvedAtTime
        );

        // 8. Payment 저장
        paymentRepository.save(payment);

        // 9. 예약 상태 변경
        reservation.reserve();

        // 10. 결제 승인 결과 반환
        return new PaymentConfirmResponseDTO(
                payment.getPaymentKey(),
                reservation.getReservationId(),
                payment.getPaymentOrderId(),
                payment.getPaymentAmount(),
                payment.getPaymentStatus(),
                payment.getPaymentMethod(),
                payment.getPaymentApprovedAt()
        );
    }

    @Transactional
    @Override
    public void cancelPayment(Long reservationId, Long refundAmount) {
        Payment payment = paymentRepository.findByReservation_ReservationId(reservationId).orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다."));

        if (payment.getPaymentStatus() != PaymentStatus.DONE) {
            throw new IllegalArgumentException("취소 할 수 없는 결제입니다.");
        }

        String encodedSecretKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        Map<String, Object> tossResponse = paymentWebClient.post()
                .uri("/v1/payments/" + payment.getPaymentKey() + "/cancel")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + encodedSecretKey
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "cancelReason", "고객 예약 취소",
                        "cancelAmount", refundAmount
                ))
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        payment.setPaymentStatus(PaymentStatus.CANCELED);
        payment.setPaymentRefundAmount(refundAmount);
        payment.setPaymentCanceledAt(LocalDateTime.now());
    }
}
