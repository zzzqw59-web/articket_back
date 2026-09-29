package com.project.articket.reservation.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "RESERVATION")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "RESERVATION_ID", nullable = false)
    private Long reservationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXHIBITION_ID", nullable = false)
    private Exhibition exhibition;

    @Column(name = "RESERVATION_PERSON", nullable = false)
    private Integer reservationPerson;

    @Column(name = "RESERVATION_DAY", nullable = false)
    private LocalDate reservationDay;

    @Column(name = "RESERVATION_STATUS", nullable = false)
    private String reservationStatus;

    @CreationTimestamp
    @Column(name = "RESERVATION_CREATED_AT", nullable = false)
    private LocalDateTime reservationCreatedAt;

    @Column(name = "RESERVATION_CANCELED_AT")
    private LocalDateTime reservationCanceledAt;

    @Column(name = "RESERVATION_AMOUNT", nullable = false)
    private Long reservationAmount;
}
