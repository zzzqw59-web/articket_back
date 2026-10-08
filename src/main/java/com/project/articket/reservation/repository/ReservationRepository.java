package com.project.articket.reservation.repository;

import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByMemberMemberId(Long memberId, Pageable pageable);

    Optional<Reservation> findByReservationOrderId(String reservationOrderId);

    List<Reservation> findByMemberMemberIdAndExhibitionExhibitionIdAndReservationStatus(
            Long memberId,
            Long exhibitionId,
            ReservationStatus reservationStatus
    );

    boolean existsByMemberMemberIdAndExhibitionExhibitionIdAndReservationDayAndReservationStatus(
            Long memberId,
            Long exhibitionId,
            LocalDate reservationDay,
            ReservationStatus reservationStatus
    );
}
