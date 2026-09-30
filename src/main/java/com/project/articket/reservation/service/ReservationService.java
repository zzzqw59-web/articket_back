package com.project.articket.reservation.service;

import com.project.articket.reservation.dto.ReservationCreateDTO;

public interface ReservationService {
    void reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO);
}
