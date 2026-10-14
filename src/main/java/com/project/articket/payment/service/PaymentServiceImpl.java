
package com.project.articket.payment.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.payment.dto.PaymentConfirmRequestDTO;
import com.project.articket.payment.dto.PaymentConfirmResponseDTO;
import com.project.articket.payment.dto.PaymentDetailResponseDTO;
import com.project.articket.payment.dto.PaymentListResponseDTO;
import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.entity.PaymentStatus;
import com.project.articket.payment.repository.PaymentRepository;
import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;
    private final WebClient paymentWebClient;
    private final MemberRepository memberRepository;
    private final TransactionTemplate transactionTemplate;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    // ======================================================
    // 결제 승인
    // ======================================================

    @Override
    public PaymentConfirmResponseDTO confirmPayment(
            Long memberId,
            PaymentConfirmRequestDTO paymentConfirmRequestDTO) {

        // Toss 승인 성공 응답을 받았는지 확인
        AtomicBoolean tossApproved = new AtomicBoolean(false);

        try {
            return transactionTemplate.execute(status ->
                    confirmPaymentInTransaction(
                            memberId,
                            paymentConfirmRequestDTO,
                            tossApproved
                    )
            );

        } catch (Exception e) {

            if (tossApproved.get()) {

                System.err.println(
                        "Toss 승인 이후 결제 처리 오류 발생"
                );

                try {
                    PaymentConsistencyStatus consistencyStatus =
                            checkPaymentConsistency(
                                    paymentConfirmRequestDTO.getOrderId()
                            );

                    switch (consistencyStatus) {

                        case CONSISTENT:
                            System.out.println(
                                    "결제 데이터가 정상적으로 저장되어 있습니다."
                            );
                            break;

                        case MISMATCH:
                            System.err.println(
                                    "결제 불일치 감지 - 복구 처리 필요"
                            );
                            break;

                        case REVIEW_REQUIRED:
                            System.err.println(
                                    "결제 상태 확인이 필요합니다."
                            );
                            break;
                    }

                } catch (Exception checkError) {
                    System.err.println(
                            "결제 정합성 검사 중 오류 발생: "
                                    + checkError.getMessage()
                    );
                }
            }

            throw e;
        }
    }

    private PaymentConfirmResponseDTO confirmPaymentInTransaction(
            Long memberId,
            PaymentConfirmRequestDTO paymentConfirmRequestDTO, AtomicBoolean tossApproved) {

        // 1. 주문 번호로 예약 소유자 ID 조회
        Long reservationMemberId = reservationRepository
                .findMemberIdByOrderId(
                        paymentConfirmRequestDTO.getOrderId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약 정보를 찾을 수 없습니다."
                        )
                );

        // 2. 예약 소유권 검증
        if (!reservationMemberId.equals(memberId)) {
            throw new AccessDeniedException(
                    "본인의 예약만 결제할 수 있습니다."
            );
        }

        // 3. 회원 비관적 락 획득
        memberRepository.findByIdForUpdate(memberId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 회원이 존재하지 않습니다."
                        )
                );

        // 4. 락 획득 후 주문 번호로 예약 다시 조회
        Reservation reservation = reservationRepository
                .findByReservationOrderId(
                        paymentConfirmRequestDTO.getOrderId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "예약 정보를 찾을 수 없습니다."
                        )
                );

        // 5. 예약 소유권 재검증
        if (!reservation.getMember().getMemberId().equals(memberId)) {
            throw new AccessDeniedException(
                    "본인의 예약만 결제할 수 있습니다."
            );
        }

        // 6. 예약 상태 확인
        if (reservation.getReservationStatus() != ReservationStatus.PENDING) {
            throw new IllegalArgumentException(
                    "결제할 수 없는 예약입니다."
            );
        }

        // 7. 결제 금액 검증
        if (!reservation.getReservationAmount()
                .equals(paymentConfirmRequestDTO.getAmount())) {
            throw new IllegalArgumentException(
                    "결제 금액이 올바르지 않습니다."
            );
        }

        // 8. Toss Secret Key를 Base64로 인코딩
        String encodedSecretKey = Base64.getEncoder()
                .encodeToString(
                        (tossSecretKey + ":")
                                .getBytes(StandardCharsets.UTF_8)
                );

        // 9. Toss 결제 승인 요청
        Map<String, Object> tossResponse;

        try {
            tossResponse = paymentWebClient.post()
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

        } catch (WebClientResponseException e) {

            System.out.println("========== Toss 승인 오류 ==========");
            System.out.println("HTTP 상태: " + e.getStatusCode());
            System.out.println("오류 응답: " + e.getResponseBodyAsString());
            System.out.println("==================================");

            throw e;
        }

        // 10. Toss 승인 응답 null 검증
        if (tossResponse == null) {
            throw new IllegalStateException(
                    "Toss 결제 승인 응답이 없습니다."
            );
        }

        // 11. Toss 승인 결과에서 필요한 값 추출
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

        String status =
                (String) tossResponse.get("status");

        // Toss 승인 성공 응답을 받은 경우
        if ("DONE".equals(status)) {
            tossApproved.set(true);
        }

        // 12. Toss 결제 승인 상태 검증
        if (!"DONE".equals(status)) {
            throw new IllegalStateException(
                    "결제가 정상적으로 승인되지 않았습니다."
            );
        }

        // 13. 주문 번호 검증
        if (!reservation.getReservationOrderId().equals(orderId)) {
            throw new IllegalStateException(
                    "결제 주문 번호가 일치하지 않습니다."
            );
        }

        // 14. 결제 금액 검증
        if (totalAmount == null ||
                !reservation.getReservationAmount().equals(
                        totalAmount.longValue()
                )) {

            throw new IllegalStateException(
                    "결제 승인 금액이 일치하지 않습니다."
            );
        }

        // 15. 결제 키 검증
        if (!paymentConfirmRequestDTO.getPaymentKey().equals(paymentKey)) {
            throw new IllegalStateException(
                    "결제 키가 일치하지 않습니다."
            );
        }

        // 16. 승인 일시 변환
        LocalDateTime approvedAtTime = null;

        if (approvedAt != null) {
            approvedAtTime = LocalDateTime.parse(
                    approvedAt.substring(0, 19)
            );
        }

        // 17. Payment 생성
        Payment payment = new Payment(
                reservation,
                orderId,
                paymentKey,
                totalAmount.longValue(),
                PaymentStatus.DONE,
                method,
                approvedAtTime
        );

        // 18. Payment 저장
        paymentRepository.save(payment);

        // 19. 예약 상태 변경
        reservation.reserve();

        // 20. 결제 승인 결과 반환
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

    // Toss 결제 상태 조회 (주문 번호 기준)
    private Map<String, Object> getTossPaymentByOrderId(String orderId) {

        String encodedSecretKey = Base64.getEncoder()
                .encodeToString(
                        (tossSecretKey + ":")
                                .getBytes(StandardCharsets.UTF_8)
                );

        return paymentWebClient.get()
                .uri("/v1/payments/orders/{orderId}", orderId)
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Basic " + encodedSecretKey
                )
                .retrieve()
                .bodyToMono(Map.class)
                .block();
    }

    // Toss 결제 상태와 Articket DB 상태 비교
    private PaymentConsistencyStatus checkPaymentConsistency(String orderId) {

        // 1. Toss 결제 상태 조회
        Map<String, Object> tossPayment =
                getTossPaymentByOrderId(orderId);

        if (tossPayment == null) {
            return PaymentConsistencyStatus.REVIEW_REQUIRED;
        }

        String tossStatus = (String) tossPayment.get("status");

        // 2. DB 예약 조회
        Reservation reservation = reservationRepository
                .findByReservationOrderId(orderId)
                .orElse(null);

        if (reservation == null) {
            System.err.println("예약 정보를 찾을 수 없습니다. orderId: " + orderId);

            return PaymentConsistencyStatus.REVIEW_REQUIRED;
        }

        // 3. DB 결제 조회
        Payment payment = paymentRepository
                .findByReservation_ReservationId(reservation.getReservationId())
                .orElse(null);

        // 4. Toss 결제 상태에 따른 판단
        if ("DONE".equals(tossStatus)) {

            // DB 결제 완료 여부
            boolean paymentCompleted =
                    payment != null
                            && payment.getPaymentStatus() == PaymentStatus.DONE
                            && orderId.equals(payment.getPaymentOrderId());

            // DB 예약 확정 여부
            boolean reservationCompleted =
                    reservation.getReservationStatus() == ReservationStatus.RESERVED;

            if (paymentCompleted && reservationCompleted) {
                System.out.println("결제 데이터 정상 일치: " + orderId);

                return PaymentConsistencyStatus.CONSISTENT;
            }

            System.err.println("결제 데이터 불일치 발생: " + orderId);
            System.err.println("Toss 상태: " + tossStatus);
            System.err.println(
                    "DB 결제 상태: " +
                            (payment == null ? "결제 기록 없음" : payment.getPaymentStatus())
            );
            System.err.println("예약 상태: " + reservation.getReservationStatus());

            return PaymentConsistencyStatus.MISMATCH;
        }

        // 5. Toss에서 취소되었거나 기타 상태인 경우
        System.err.println(
                "Toss 결제 상태 추가 확인 필요: "
                        + tossStatus
                        + ", orderId: "
                        + orderId
        );

        return PaymentConsistencyStatus.REVIEW_REQUIRED;
    }

    // ======================================================
    // 결제 취소
    // ======================================================

    @Transactional
    @Override
    public void cancelPayment(Long reservationId, Long refundAmount) {

        Payment payment = paymentRepository
                .findByReservation_ReservationId(reservationId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "결제 정보를 찾을 수 없습니다."
                        )
                );

        if (payment.getPaymentStatus() != PaymentStatus.DONE) {
            throw new IllegalArgumentException(
                    "취소할 수 없는 결제입니다."
            );
        }

        String encodedSecretKey = Base64.getEncoder()
                .encodeToString(
                        (tossSecretKey + ":")
                                .getBytes(StandardCharsets.UTF_8)
                );

        paymentWebClient.post()
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

    // ======================================================
    // 내 결제 내역 페이징 / 검색 / 정렬 조회
    // ======================================================

    @Override
    public PageResponseDTO<PaymentListResponseDTO> getMyPaymentList(
            Long memberId,
            PageRequestDTO pageRequestDTO) {

        // 정렬 조건 설정 (기본값: 최신순 DESC)
        Sort sort = "asc".equalsIgnoreCase(pageRequestDTO.getSort())
                ? Sort.by("paymentCreatedAt").ascending()
                : Sort.by("paymentCreatedAt").descending();

        Pageable pageable = PageRequest.of(
                pageRequestDTO.getPage() - 1,
                pageRequestDTO.getSize(),
                sort
        );

        Page<Payment> result;

        String searchType = pageRequestDTO.getSearchType();
        String keyword = pageRequestDTO.getKeyword();

        // 검색 조건 분기 처리
        if (keyword != null && !keyword.trim().isEmpty()) {

            if ("orderId".equals(searchType)) {
                result = paymentRepository
                        .findByReservation_Member_MemberIdAndPaymentOrderIdContaining(
                                memberId, keyword, pageable
                        );
            } else {
                // 기본값: 전시명 검색
                result = paymentRepository
                        .findByReservation_Member_MemberIdAndReservation_Exhibition_ExhibitionTitleContaining(
                                memberId, keyword, pageable
                        );
            }

        } else {
            result = paymentRepository
                    .findByReservation_Member_MemberId(
                            memberId, pageable
                    );
        }

        // DTO 변환
        List<PaymentListResponseDTO> dtoList = result.getContent()
                .stream()
                .map(payment -> new PaymentListResponseDTO(
                        payment.getPaymentId(),
                        payment.getPaymentOrderId(),
                        payment.getReservation()
                                .getExhibition()
                                .getExhibitionTitle(),
                        payment.getPaymentAmount(),
                        payment.getPaymentStatus(),
                        payment.getPaymentCreatedAt()
                ))
                .collect(Collectors.toList());

        return new PageResponseDTO<>(
                dtoList,
                pageRequestDTO,
                result.getTotalElements()
        );
    }

    // ======================================================
    // 결제 단건 상세 조회
    // ======================================================

    @Override
    public PaymentDetailResponseDTO getPaymentDetail(
            Long memberId,
            Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "해당 결제 내역이 존재하지 않습니다. ID: " + paymentId
                        )
                );

        // 결제 소유권 검증
        if (!payment.getReservation()
                .getMember()
                .getMemberId()
                .equals(memberId)) {

            throw new AccessDeniedException(
                    "본인의 결제 내역만 조회할 수 있습니다."
            );
        }

        return new PaymentDetailResponseDTO(
                payment.getPaymentId(),
                payment.getPaymentOrderId(),
                payment.getReservation().getReservationId(),
                payment.getPaymentAmount(),
                payment.getPaymentStatus(),
                payment.getPaymentApprovedAt(),
                payment.getPaymentMethod(),
                payment.getPaymentRefundAmount(),
                payment.getPaymentCreatedAt(),
                payment.getPaymentCanceledAt()
        );
    }
}
