package com.project.articket.notification.service;

import com.project.articket.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 모든 알림 일괄 읽음 처리
    @Transactional
    public void readAllNotifications(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId, LocalDateTime.now());
    }


    // 모든 알림 일괄 삭제
    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteAllByMemberId(memberId);
    }
}