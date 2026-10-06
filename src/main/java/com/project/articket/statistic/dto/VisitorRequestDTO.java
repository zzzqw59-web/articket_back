package com.project.articket.statistic.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class VisitorRequestDTO {
    private Long exhibitionId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
