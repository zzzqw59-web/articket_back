package com.project.articket.mypage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyReplyListResponseDTO {

    private Long replyId;            // 댓글 PK
    private String replyType;        // "REVIEW" 또는 "ASK" (프론트에서 URL 분기용)
    private Long targetId;           // 원본 글 PK (reviewId 또는 askId)
    private String replyContent;     // 댓글 내용 (화면의 '댓글 내용' 컬럼)

    private LocalDateTime replyCreatedAt;  // 생성일
    private LocalDateTime replyModifiedAt; // 수정일

    // 프론트에 전달할 최종 일자 반환 (수정되었으면 수정일, 아니면 생성일)
    public LocalDateTime getDisplayDate() {
        if (replyModifiedAt != null && replyCreatedAt != null && replyModifiedAt.isAfter(replyCreatedAt)) {
            return replyModifiedAt;
        }
        return replyCreatedAt;
    }
}