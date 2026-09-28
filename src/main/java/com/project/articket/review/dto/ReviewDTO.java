package com.project.articket.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private String memberName;
    private String exhibitionTitle;
    private String reviewTitle;
    private String reviewBody;
    private LocalDateTime reviewCreatedAt;
    private LocalDateTime reviewModifiedAt;
    private Integer reviewHits;
}
