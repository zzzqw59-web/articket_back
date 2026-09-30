package com.project.articket.reservation.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCreateDTO {
    @NotNull(message = "관람할 전시를 선택해주세요.")
    private Long exhibitionId;

    @NotNull(message = "관람할 인원수를 선택해주세요.")
    @Min(value = 1, message = "예약 인원은 1명 이상이어야 합니다.")
    private Integer reservationPerson;

    @NotNull(message = "관람할 전시의 날짜를 선택해주세요.")
    private LocalDate reservationDay;
}
