package com.project.articket.exhibition.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ExhibitionUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotNull(message = "전시 시작일은 필수 입니다.")
    private LocalDate startDate;

    @NotNull(message = "전시 종료일은 필수 입니다.")
    private LocalDate endDate;

    private String url;

    @NotBlank(message = "지역은 필수입니다.")
    private String area;

    @NotBlank(message = "가격은 필수입니다.")
    private String price;

    private Integer ticketPrice; // 유료 선택 시에만 프론트에서 필수 검증

    private String description;
}
