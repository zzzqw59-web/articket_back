package com.project.articket.notification.repository;

import com.project.articket.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 1. 특정 회원의 전체 알림 목록 조회 (최신순 페이징)
    Page<Notification> findByMemberId_MemberIdOrderByNotificationIdDesc(Long memberId, Pageable pageable);

    // 2. 특정 회원의 읽지 않은 알림 목록 조회 (최신순)
    List<Notification> findByMemberId_MemberIdAndNotificationIsReadOrderByNotificationIdDesc(Long memberId, Integer notificationIsRead);

    // 3. 특정 회원의 읽지 않은 알림 개수 조회 (알림 배지/N 표시용)
    long countByMemberId_MemberIdAndNotificationIsRead(Long memberId, Integer notificationIsRead);

    // 4. 특정 회원의 모든 안 읽은 알림을 '일괄 읽음' 처리
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n " +
            "SET n.notificationIsRead = 1, n.notificationReadAt = :now " +
            "WHERE n.memberId.memberId = :memberId AND n.notificationIsRead = 0")
    int markAllAsReadByMemberId(@Param("memberId") Long memberId, @Param("now") LocalDateTime now);

    // 5. 특정 회원의 모든 알림 일괄 삭제
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notification n WHERE n.memberId.memberId = :memberId")
    int deleteAllByMemberId(@Param("memberId") Long memberId);
}