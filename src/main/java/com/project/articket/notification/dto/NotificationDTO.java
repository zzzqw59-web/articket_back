package com.project.articket.notification.dto;

import com.project.articket.notification.entity.Notification;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private Long memberId;
    private Integer notificationType;      // 0: 새 문의, 1: 리뷰 댓글, 2: 문의 댓글
    private Long notificationTargetId;   // REVIEW_ID 또는 ASK_ID
    private Integer notificationIsRead;    // 0: 안읽음, 1: 읽음
    private LocalDateTime notificationCreatedAt;
    private LocalDateTime notificationReadAt;

    public static NotificationDTO from(Notification entity) {
        return NotificationDTO.builder()
                .memberId(entity.getMemberId().getMemberId())
                .notificationType(entity.getNotificationType())
                .notificationTargetId(entity.getNotificationTargetId())
                .notificationIsRead(entity.getNotificationIsRead())
                .notificationCreatedAt(entity.getNotificationCreatedAt())
                .notificationReadAt(entity.getNotificationReadAt())
                .build();
    }
}