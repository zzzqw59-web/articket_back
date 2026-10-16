package com.project.articket.reservation.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.reservation.dto.ReservationCancelDTO;
import com.project.articket.reservation.dto.ReservationCreateDTO;
import com.project.articket.reservation.dto.ReservationDTO;
import com.project.articket.reservation.dto.ReservationListDTO;
import com.project.articket.reservation.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    private void validatePageRequest(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPage() < 1) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 번호는 1 이상이어야 합니다."
            );
        }

        if (pageRequestDTO.getSize() < 1 || pageRequestDTO.getSize() > 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "페이지 크기는 1 이상 100 이하여야 합니다."
            );
        }
    }

    @GetMapping("/me")
    public PageResponseDTO<ReservationDTO> reservationList(Authentication authentication, PageRequestDTO pageRequestDTO) {
        // 페이징 입력값 검증
        validatePageRequest(pageRequestDTO);
        Long memberId = (Long) authentication.getPrincipal();

        PageResponseDTO<ReservationDTO> dto = reservationService.reservationList(memberId, pageRequestDTO);
        return dto;
    }

    @GetMapping("/{reservationId}")
    public ReservationDTO reservationDetail(@PathVariable Long reservationId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return reservationService.reservationDetail(memberId, reservationId);
    }

    @GetMapping("/order/{orderId}")
    public ReservationDTO reservationDetailByOrderId(@PathVariable String orderId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        return reservationService.reservationDetailByOrderId(memberId, orderId);
    }

    @PostMapping
    public ReservationDTO reservationCreate(@Valid @RequestBody ReservationCreateDTO reservationCreateDTO, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();

        return reservationService.reservationCreate(memberId, reservationCreateDTO);
    }

    @DeleteMapping("/{reservationId}/cancel")
    void reservationCancel(@PathVariable Long reservationId, @Valid @RequestBody ReservationCancelDTO reservationCancelDTO, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reservationService.reservationCancel(memberId, reservationId, reservationCancelDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public PageResponseDTO<ReservationListDTO> reservationListForAdmin(PageRequestDTO pageRequestDTO, Authentication authentication) {
        // 페이징 입력값 검증
        validatePageRequest(pageRequestDTO);
        return reservationService.reservationListForAdmin(pageRequestDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{reservationId}/cancel/admin")
    void cancelReservation(@PathVariable Long reservationId, @RequestBody ReservationCancelDTO reservationCancelDTO){
        reservationService.cancelReservation(reservationId, reservationCancelDTO.getCancelReason(), reservationCancelDTO.getCancelDetail());

    }
}
