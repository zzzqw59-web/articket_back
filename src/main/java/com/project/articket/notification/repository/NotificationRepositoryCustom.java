package com.project.articket.notification.repository;

import com.project.articket.notification.entity.Notification;

import java.util.List;

public interface NotificationRepositoryCustom {
    // 특정 회원의 읽지 않은 알림 목록 조회
    List<Notification> findUnreadNotificationsByMember(Long memberId);
}