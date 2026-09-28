package com.project.articket.notification.entity;

import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NOTIFICATION_ID")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 회원 삭제 시 연관 알림 일괄 삭제
    private Member memberId;

    @Column(name = "NOTIFICATION_TYPE", nullable = false)
    private Integer notificationType; // 0: 새 문의, 1: 리뷰 댓글, 2: 문의 댓글

    @Column(name = "NOTIFICATION_TARGET_ID", nullable = false)
    private Long notificationTargetId; // REVIEW_ID 또는 ASK_ID

    @Column(name = "NOTIFICATION_IS_READ", nullable = false)
    @Builder.Default
    private Integer notificationIsRead = 0; // 0: 안읽음, 1: 읽음

    @CreationTimestamp
    @Column(name = "NOTIFICATION_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime notificationCreatedAt;

    @Column(name = "NOTIFICATION_READ_AT")
    private LocalDateTime notificationReadAt;

    // 알림 읽음 처리 비즈니스 메서드
    public void read() {
        this.notificationIsRead = 1;
        this.notificationReadAt = LocalDateTime.now();
    }
}