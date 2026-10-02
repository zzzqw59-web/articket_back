package com.project.articket.statistic.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalProfitDTO {
    private Long exhibitionId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long profit;
}
