package com.project.articket.payment.entity;

import com.project.articket.reservation.entity.Reservation;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "PAYMENT", uniqueConstraints = {
        @UniqueConstraint(name = "ORDER_ID_UNIQUE", columnNames = "PAYMENT_ORDER_ID"),
        @UniqueConstraint(name = "PAYMENT_KEY_UNIQUE", columnNames = "PAYMENT_KEY"),
        @UniqueConstraint(name = "RESERVATION_ID_UNIQUE", columnNames = "RESERVATION_ID")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PAYMENT_ID", nullable = false)
    private Long paymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false)
    private Reservation reservation;

    @Column(name = "PAYMENT_ORDER_ID", nullable = false, length = 64)
    private String paymentOrderId;

    @Column(name = "PAYMENT_KEY", nullable = false, length = 200)
    private String paymentKey;

    @Column(name = "PAYMENT_AMOUNT", nullable = false)
    private Long paymentAmount;

    @Column(name = "PAYMENT_REFUND_AMOUNT")
    private Long paymentRefundAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_STATUS", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "PAYMENT_METHOD", length = 50)
    private String paymentMethod;

    @Column(name = "PAYMENT_APPROVED_AT")
    private LocalDateTime paymentApprovedAt;

    @CreationTimestamp
    @Column(name = "PAYMENT_CREATED_AT", nullable = false)
    private LocalDateTime paymentCreatedAt;

    @Column(name = "PAYMENT_CANCELED_AT")
    private LocalDateTime paymentCanceledAt;

    public Payment(Reservation reservation, String paymentOrderId, String paymentKey, Long paymentAmount, PaymentStatus paymentStatus, String paymentMethod, LocalDateTime paymentApprovedAt) {
        this.reservation = reservation;
        this.paymentOrderId = paymentOrderId;
        this.paymentKey = paymentKey;
        this.paymentAmount = paymentAmount;
        this.paymentStatus = paymentStatus;
        this.paymentMethod = paymentMethod;
        this.paymentApprovedAt = paymentApprovedAt;
    }
}
