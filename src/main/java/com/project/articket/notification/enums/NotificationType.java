package com.project.articket.notification.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    // 0X: 회원 상태 관련
    MEMBER_DEACTIVE(0, "회원 비활성화"),
    MEMBER_ACTIVE(1, "회원 활성화"),

    // 1X: 문의 관련
    ASK_NEW(10, "새 문의 (관리자)"),
    ASK_REPLY(11, "문의에 댓글"),
    ASK_UPDATE(12, "관리자에 의한 문의 수정"),
    ASK_DELETE(13, "관리자에 의한 문의 삭제"),
    ASK_REPLY_UPDATE(14, "관리자에 의한 문의 댓글 수정"),
    ASK_REPLY_DELETE(15, "관리자에 의한 문의 댓글 삭제"),

    // 2X: 리뷰 관련
    REVIEW_NEW(20, "새 리뷰 (전시 관계자)"),
    REVIEW_REPLY(21, "리뷰에 댓글"),
    REVIEW_UPDATE(22, "관리자에 의한 리뷰 수정"),
    REVIEW_DELETE(23, "관리자에 의한 리뷰 삭제"),
    REVIEW_REPLY_UPDATE(24, "관리자에 의한 리뷰 댓글 수정"),
    REVIEW_REPLY_DELETE(25, "관리자에 의한 리뷰 댓글 삭제");

    private final int code;
    private final String description;
}