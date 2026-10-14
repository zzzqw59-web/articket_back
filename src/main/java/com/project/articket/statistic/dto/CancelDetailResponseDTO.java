package com.project.articket.statistic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelDetailResponseDTO {
    private LocalDateTime cancelDate;
    private String reason;
}
