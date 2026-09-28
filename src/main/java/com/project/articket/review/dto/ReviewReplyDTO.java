package com.project.articket.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReplyDTO {
    private Long reviewReplyId; // 댓글 식별
    private Long reviewId; // 어느 리뷰의 댓글인지
    private String reviewReplyBody;
    private String memberName;
    private LocalDateTime reviewReplyCreatedAt;
}
