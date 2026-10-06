package com.project.articket.notification.repository;

import com.project.articket.notification.entity.Notification;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.project.articket.notification.entity.QNotification.notification;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findUnreadNotificationsByMember(Long memberId) {
        return queryFactory
                .selectFrom(notification)
                .where(
                        notification.memberId.memberId.eq(memberId),
                        notification.notificationIsRead.eq(0) // 0: 안읽음
                )
                .orderBy(notification.notificationCreatedAt.desc())
                .fetch();
    }
}