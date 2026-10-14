package com.project.articket.review.dto;

import com.project.articket.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.project.articket.common.util.DateTimeUtils.toDateTimeSecondString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReviewListResponseDTO {

    private Long reviewId;
    private String exhibitionTitle;    // 전시회 제목
    private String reviewTitle;        // 리뷰 제목
    private Integer reviewHits;        // 조회수
    private int replyCount;            // 댓글 수
    private boolean hasImage;          // 첨부 이미지 존재 여부
    private String reviewCreatedAt;    // 작성일시
    private String reviewModifiedAt;   // 수정일시

    // 💡 QueryDSL Projections.constructor() 매핑 전용 생성자 추가
    public MyReviewListResponseDTO(
            Long reviewId,
            String exhibitionTitle,
            String reviewTitle,
            Integer reviewHits,
            int replyCount,
            boolean hasImage,
            LocalDateTime reviewCreatedAt,
            LocalDateTime reviewModifiedAt
    ) {
        this.reviewId = reviewId;
        this.exhibitionTitle = exhibitionTitle;
        this.reviewTitle = reviewTitle;
        this.reviewHits = reviewHits;
        this.replyCount = replyCount;
        this.hasImage = hasImage;
        this.reviewCreatedAt = toDateTimeSecondString(reviewCreatedAt);
        this.reviewModifiedAt = toDateTimeSecondString(reviewModifiedAt);
    }

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