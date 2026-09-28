package com.project.articket.notification.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.notification.dto.NotificationCreateDTO;
import com.project.articket.notification.dto.NotificationDTO;

import java.util.List;

public interface NotificationService {

    // 알림 생성 (도메인 이벤트/서비스 연동용)
    Long createNotification(NotificationCreateDTO createDto);

    // 내 알림 목록 페이징 조회
    PageResponseDTO<NotificationDTO> getMyNotifications(Long memberId, PageRequestDTO pageRequestDTO);

    // 안 읽은 알림 목록 조회
    List<NotificationDTO> getUnreadNotifications(Long memberId);

    // 안 읽은 알림 개수 (배지용)
    long getUnreadCount(Long memberId);

    // 단건 알림 읽음 처리
    void readNotification(Long notificationId, Long memberId);

    // 내 모든 알림 일괄 읽음 처리
    void readAllNotifications(Long memberId);

    // 단건 알림 삭제
    void deleteNotification(Long notificationId, Long memberId);

    // 내 모든 알림 일괄 삭제
    void deleteAllNotifications(Long memberId);
}