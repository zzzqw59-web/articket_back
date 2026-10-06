package com.project.articket.reservation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationDTO {
    private Long reservationId;
    private String reservationOrderId;
    private String exhibitionTitle;
    private String exhibitionArea;
    private LocalDate reservationDay;
    private Integer reservationPerson;
    private Long reservationAmount;
    private LocalDateTime reservationCreatedAt;
    private String reservationStatus;
    private LocalDateTime reservationCanceledAt;


}
