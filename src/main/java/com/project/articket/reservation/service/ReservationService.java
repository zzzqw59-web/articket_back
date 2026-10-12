package com.project.articket.reservation.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.reservation.dto.ReservationCancelDTO;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.entity.ReservationCancelReason;
import com.project.articket.review.dto.ReviewAvailableExhibitionDTO;

import java.util.List;

public interface ReservationService {
    ReservationDTO reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO);

    PageResponseDTO<ReservationDTO> reservationList(Long memberId, PageRequestDTO pageRequestDTO);

    ReservationDTO reservationDetail(Long reservationId);

    ReservationDTO reservationDetailByOrderId(String orderId);

    void reservationCancel(Long memberId, Long reservationId, ReservationCancelDTO reservationCancelDTO);

    void reserveReservation(Long reservationId);

    void cancelReservation(Long reservationId, ReservationCancelReason reason, String detail);

    List<ReviewAvailableExhibitionDTO> getAvailableExhibitionsForReview(Long memberId);
}
