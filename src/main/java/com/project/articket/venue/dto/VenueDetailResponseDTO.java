package com.project.articket.venue.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VenueDetailResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String photoUrl;
    private Double latitude;
    private Double longitude;
    private String tel;
    private String honePageUrl;
    private List<VenueExhibitionItemDTO> exhibitions;
}
