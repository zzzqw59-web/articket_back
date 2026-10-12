        package com.project.articket.reservation.repository;

import com.project.articket.reservation.entity.Reservation;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.review.dto.ReviewAvailableExhibitionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("""
        SELECT DISTINCT new com.project.articket.review.dto.ReviewAvailableExhibitionDTO(
            r.exhibition.exhibitionId,
            r.exhibition.exhibitionTitle
        )
        FROM Reservation r
        WHERE r.member.memberId = :memberId
          AND r.reservationStatus = com.project.articket.reservation.entity.ReservationStatus.RESERVED
          AND r.reservationDay <= :today
          AND NOT EXISTS (
              SELECT rv.reviewId
              FROM Review rv
              WHERE rv.member.memberId = :memberId
                AND rv.exhibition.exhibitionId = r.exhibition.exhibitionId
          )
        """)
    List<ReviewAvailableExhibitionDTO> findAvailableExhibitionsForReview(
            @Param("memberId") Long memberId,
            @Param("today") LocalDate today
    );

    Page<Reservation> findByMemberMemberIdAndExhibitionExhibitionTitleContaining(
            Long memberId,
            String exhibitionTitle,
            Pageable pageable
    );

    Page<Reservation> findByMemberMemberIdAndReservationOrderIdContaining(
            Long memberId,
            String reservationOrderId,
            Pageable pageable
    );
}
