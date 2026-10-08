package com.project.articket.review.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.project.articket.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long reviewId;
    private String memberNickname;
    private String exhibitionTitle;
    private String reviewTitle;
    private String reviewBody;
    private LocalDateTime reviewCreatedAt;
    private LocalDateTime reviewModifiedAt;
    private Integer reviewHits;
    private List<ReviewImageDTO> images;
}
