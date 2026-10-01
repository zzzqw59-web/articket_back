package com.project.articket.exhibition.dto;

import com.project.articket.venue.dto.VenueSummaryDTO;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Builder
public class ExhibitionDetailResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String url;
    private String imgUrl;
    private LocalDate startDate;
    private LocalDate endDate;
    private String area;
    private String price;
    private Integer ticketPrice; //유료만 값 존재, 무료는 null값
    private boolean free;
    private VenueSummaryDTO venue;
}
