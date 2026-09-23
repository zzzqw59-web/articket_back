package com.project.articket.notification.service;

public interface NotificationService {
    void readAllNotifications(Long memberId);
    void deleteAllNotifications(Long memberId);
}
