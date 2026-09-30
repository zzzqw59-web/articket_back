package com.project.articket.reservation.service;

import com.project.articket.exhibition.repository.ExhibitionRepository;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final ExhibitionRepository exhibitionRepository;

    @Override
    public void reservationCreate(Long memberId, ReservationCreateDTO reservationCreateDTO) {

    }
}
