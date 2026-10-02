package com.project.articket.reservation.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;

public interface ReservationService {
    void reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO);

    PageResponseDTO<ReservationDTO> reservationPage(Long memberId, PageRequestDTO pageRequestDTO);

    ReservationDTO reservationDetail(Long reservationId);

    void reservationCancel(Long memberId, Long reservationId);

    void reserveReservation(Long reservationId);

    void cancelReservation(Long reservationId);
}
