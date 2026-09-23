package com.project.articket.venue.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
public class VenueSummaryDTO {
    private Long id;
    private String name;
    private String photoUrl;
    private String tel;
    private long ongoingCount;
}
