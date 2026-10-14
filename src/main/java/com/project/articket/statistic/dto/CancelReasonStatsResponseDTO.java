package com.project.articket.statistic.dto;

import com.project.articket.reservation.entity.ReservationCancelReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelReasonStatsResponseDTO {
    private ReservationCancelReason type;
    private Long value;
}
