package com.project.articket.reservation.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    @GetMapping("/me")
    public PageResponseDTO<ReservationDTO> reservationList(Authentication authentication, PageRequestDTO pageRequestDTO) {
        Long memberId = (Long) authentication.getPrincipal();
        PageResponseDTO<ReservationDTO> dto = reservationService.reservationPage(memberId, pageRequestDTO);
        return dto;
    }

    @GetMapping("/{reservationId}")
    public ReservationDTO reservationDetail(@PathVariable Long reservationId) {
        ReservationDTO reservationDTO = reservationService.reservationDetail(reservationId);
        return reservationDTO;
    }

    @PostMapping
    void reservationCreate(@Valid @RequestBody ReservationCreateDTO reservationCreateDTO, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reservationService.reservationCreate(memberId, reservationCreateDTO);
    }

    @DeleteMapping("/{reservationId}/cancel")
    void reservationCancel(@PathVariable Long reservationId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reservationService.reservationCancel(memberId, reservationId);
    }
}
