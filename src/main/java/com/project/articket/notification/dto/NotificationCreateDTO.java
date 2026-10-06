package com.project.articket.notification.dto;

import com.project.articket.member.entity.Member;
import com.project.articket.notification.entity.Notification;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateDTO {

    private Member receiver;              // 알림 수신 회원 Entity
    private Integer notificationType;     // 0: 새 문의, 1: 리뷰 댓글, 2: 문의 댓글
    private Long notificationTargetId;  // target ID (ASK_ID 등)

    public Notification toEntity() {
        return Notification.builder()
                .memberId(this.receiver)
                .notificationType(this.notificationType)
                .notificationTargetId(this.notificationTargetId)
                .notificationIsRead(0)
                .build();
    }
}