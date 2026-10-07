package com.project.articket.withdraw.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WithdrawStatus {

    IN_PROGRESS("IN_PROGRESS", "탈퇴 진행 중"),
    COMPLETED("COMPLETED", "탈퇴 완료"),
    CANCELED("CANCELED", "탈퇴 취소");

    private final String key;
    private final String title;
}