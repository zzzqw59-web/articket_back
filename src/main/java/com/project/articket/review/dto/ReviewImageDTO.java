package com.project.articket.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReviewImageDTO {
    private Long reviewImageId;
    private String reviewImageOrigin;
    private String reviewImageUrl;
    private Integer reviewImageOrder;
}
