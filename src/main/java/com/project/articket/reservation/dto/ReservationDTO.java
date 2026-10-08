package com.project.articket.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.articket.exhibition.entity.Exhibition;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationCreatedAt;
    private String reservationStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime reservationCanceledAt;
    private Long exhibitionId;
}
