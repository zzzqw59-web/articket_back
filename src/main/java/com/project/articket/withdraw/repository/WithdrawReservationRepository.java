
package com.project.articket.withdraw.repository;

import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;

public interface WithdrawReservationRepository
        extends JpaRepository<Reservation, Long> {

    boolean existsByMemberMemberIdAndReservationStatusInAndReservationDayGreaterThanEqual(
            Long memberId,
            Collection<ReservationStatus> reservationStatuses,
            LocalDate reservationDay
    );
}
