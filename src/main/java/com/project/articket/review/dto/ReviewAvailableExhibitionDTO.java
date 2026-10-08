package com.project.articket.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewAvailableExhibitionDTO {
    private Long exhibitionId;
    private String exhibitionTitle;
}
