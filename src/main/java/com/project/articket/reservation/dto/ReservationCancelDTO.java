package com.project.articket.reservation.dto;

import com.project.articket.reservation.entity.ReservationCancelReason;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservationCancelDTO {
    @NotNull(message = "취소 사유는 필수입니다.")
    private ReservationCancelReason cancelReason;
    @Size(max = 500, message = "취소 상세 사유는 500자 이내로 입력 가능합니다.")
    private String cancelDetail;
}
