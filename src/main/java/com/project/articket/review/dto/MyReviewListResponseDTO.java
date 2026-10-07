package com.project.articket.review.dto;

import com.project.articket.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
public class MyReviewListResponseDTO {

    private Long reviewId;
    private String exhibitionTitle;    // 전시회 제목
    private String reviewTitle;        // 리뷰 제목
    private Integer reviewHits;        // 조회수
    private int replyCount;            // 댓글 수 👈 추가
    private boolean hasImage;          // 첨부 이미지 존재 여부
    private String reviewCreatedAt;    // 작성일시
    private String reviewModifiedAt;   // 수정일시

    public static MyReviewListResponseDTO from(Review review, boolean hasImage, int replyCount) {
        return MyReviewListResponseDTO.builder()
                .reviewId(review.getReviewId())
                .exhibitionTitle(review.getExhibition() != null ? review.getExhibition().getExhibitionTitle() : null)
                .reviewTitle(review.getReviewTitle())
                .reviewHits(review.getReviewHits())
                .replyCount(replyCount)
                .hasImage(hasImage)
                .reviewCreatedAt(toDateTimeSecondString(review.getReviewCreatedAt()))
                .reviewModifiedAt(toDateTimeSecondString(review.getReviewModifiedAt()))
                .build();
    }
}