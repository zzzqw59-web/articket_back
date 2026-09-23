package com.project.articket.exhibition.dto;


import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class ExhibitionListItemDTO {
    private Long id;
    private String title;
    private String imgUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String venueName;
    private boolean free;
}
