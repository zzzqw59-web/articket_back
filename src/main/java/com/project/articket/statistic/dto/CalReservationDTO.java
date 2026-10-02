package com.project.articket.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalReservationDTO {
    private Long exhibitionId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long reservation;
}
