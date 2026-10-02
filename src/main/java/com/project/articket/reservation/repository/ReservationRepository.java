package com.project.articket.reservation.repository;

import com.project.articket.reservation.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Page<Reservation> findByMemberMemberId(Long memberId, Pageable pageable);

}
