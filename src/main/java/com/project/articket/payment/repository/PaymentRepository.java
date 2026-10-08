package com.project.articket.payment.repository;

import com.project.articket.payment.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findByReservation_Member_MemberId(Long memberId, Pageable pageable);

    Page<Payment> findByReservation_Member_MemberIdAndPaymentOrderIdContaining(Long memberId, String paymentOrderId, Pageable pageable);

    Page<Payment> findByReservation_Member_MemberIdAndReservation_Exhibition_ExhibitionTitleContaining(Long memberId, String exhibitionTitle, Pageable pageable);

    Optional<Payment> findByReservation_ReservationId(Long reservationId);
}
