package com.project.articket.reservation.controller;

import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping
    void reservationCreate(@RequestParam Long memberId, @Valid @RequestBody  ReservationCreateDTO reservationCreateDTO) {
        reservationService.reservationCreate(memberId, reservationCreateDTO);
    }
}
