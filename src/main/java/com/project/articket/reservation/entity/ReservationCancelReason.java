package com.project.articket.reservation.entity;

public enum ReservationCancelReason {
    PERSONAL, // 개인 사정
    SCHEDULE, // 일정 변경
    PLAN_CHANGE, // 관람 계획 변경
    COMPANION, // 동행인 사정
    WRONG_RESERVATION, // 예약 정보 오류
    OTHER // 기타
}
