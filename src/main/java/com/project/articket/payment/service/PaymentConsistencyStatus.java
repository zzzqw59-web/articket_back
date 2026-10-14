package com.project.articket.payment.service;

public enum PaymentConsistencyStatus {
    CONSISTENT, // Toss와 DB 정상 일치
    MISMATCH, // Toss와 DB 불일치
    REVIEW_REQUIRED, // 상태 확인 또는 수동 검토 필요
}
