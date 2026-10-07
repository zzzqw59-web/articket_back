package com.project.articket.withdraw.repository;

import com.project.articket.payment.entity.Payment;
import com.project.articket.payment.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface WithdrawPaymentRepository
        extends JpaRepository<Payment, Long> {

    boolean existsByReservation_Member_MemberIdAndPaymentStatusIn(
            Long memberId,
            Collection<PaymentStatus> paymentStatuses
    );

    boolean existsByReservation_Member_MemberId(
            Long memberId
    );
}