package com.project.articket.reservation.service;

import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import org.springframework.data.domain.Page;

public interface ReservationService {
    void reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO);

    Page<ReservationDTO> reservationPage(Long memberId, ReservationDTO reservationDTO);
}
