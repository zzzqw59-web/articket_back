package com.project.articket.staff.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StaffExhibitionResponseDTO {

    private Long exhibitionId;
    private String exhibitionTitle;
    private LocalDateTime staffCreatedAt;
}