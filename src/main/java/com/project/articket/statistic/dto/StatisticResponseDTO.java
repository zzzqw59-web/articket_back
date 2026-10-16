package com.project.articket.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StatisticResponseDTO {
    private LocalDate anchorDate;
    private Long value;
}
