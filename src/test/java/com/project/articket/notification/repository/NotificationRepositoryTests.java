package com.project.articket.notification.repository;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.notification.entity.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Rollback(false) // DB에 더미 데이터를 남아있게 하기 위해 false로 설정
class NotificationRepositoryTests {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("알림 더미 데이터 생성 및 읽음/카운트/일괄읽음/일괄삭제 테스트")
    void testNotificationCrudAndDummyData() {
        // --------------------------------------------------
        // 1. 테스트용 회원 조회 (기존 데이터가 없으면 새로 생성)
        // --------------------------------------------------
        Member member1 = memberRepository.findByMemberEmail("user1@articket.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .memberEmail("user1@articket.com")
                        .memberPassword("password123!")
                        .memberName("홍길동")
                        .memberNickname("티켓왕")
                        .memberPhone("010-1234-4567")
                        .memberJoinCreatedAt(LocalDateTime.now())
                        .build()));

        Member member2 = memberRepository.findByMemberEmail("user2@articket.com")
                .orElseGet(() -> memberRepository.save(Member.builder()
                        .memberEmail("user2@articket.com")
                        .memberPassword("password123!")
                        .memberName("김철수")
                        .memberNickname("전시매니아")
                        .memberPhone("010-9876-5432")
                        .memberJoinCreatedAt(LocalDateTime.now())
                        .build()));

        // --------------------------------------------------
        // 2. 더미 알림 데이터 생성 (member1: 3개, member2: 1개)
        // notificationType: 0(새 문의), 1(리뷰 댓글), 2(문의 댓글)
        // --------------------------------------------------
        Notification noti1 = Notification.builder()
                .memberId(member1)
                .notificationType(2) // 문의 댓글 등록
                .notificationTargetId(101L) // Ask ID 101
                .notificationIsRead(0) // 안 읽음
                .build();

        Notification noti2 = Notification.builder()
                .memberId(member1)
                .notificationType(1) // 리뷰 댓글 등록
                .notificationTargetId(202L) // Review ID 202
                .notificationIsRead(0) // 안 읽음
                .build();

        Notification noti3 = Notification.builder()
                .memberId(member1)
                .notificationType(0) // 새 문의 등록 알림
                .notificationTargetId(102L) // Ask ID 102
                .notificationIsRead(1) // 이미 읽음
                .notificationReadAt(LocalDateTime.now())
                .build();

        Notification noti4 = Notification.builder()
                .memberId(member2)
                .notificationType(2) // 문의 댓글 등록
                .notificationTargetId(103L) // Ask ID 103
                .notificationIsRead(0) // 안 읽음
                .build();

        notificationRepository.saveAll(List.of(noti1, noti2, noti3, noti4));

        // --------------------------------------------------
        // 3. 검증 및 기능 테스트
        // --------------------------------------------------

        // [검증 1] member1의 읽지 않은 알림 개수 조회 (noti1, noti2 총 2개)
        long unreadCount = notificationRepository.countByMemberId_MemberIdAndNotificationIsRead(member1.getMemberId(), 0);
        assertThat(unreadCount).isEqualTo(2L);

        // [검증 2] member1의 전체 알림 페이징 조회 (총 3개)
        Page<Notification> pageResult = notificationRepository.findByMemberId_MemberIdOrderByNotificationIdDesc(
                member1.getMemberId(), PageRequest.of(0, 10)
        );
        assertThat(pageResult.getTotalElements()).isEqualTo(3);

        // [검증 3] 단건 알림 읽음 처리 메서드 테스트 (noti1.read())
        noti1.read(); // isRead = 1, readAt = now
        notificationRepository.save(noti1);

        long unreadCountAfterSingleRead = notificationRepository.countByMemberId_MemberIdAndNotificationIsRead(member1.getMemberId(), 0);
        assertThat(unreadCountAfterSingleRead).isEqualTo(1L); // noti2 1개 남음

        // [검증 4] 남아있는 모든 안 읽은 알림 일괄 읽음 처리
        int updatedRows = notificationRepository.markAllAsReadByMemberId(member1.getMemberId(), LocalDateTime.now());
        assertThat(updatedRows).isEqualTo(1); // noti2가 1개였으므로 1건 업데이트

        long finalUnreadCount = notificationRepository.countByMemberId_MemberIdAndNotificationIsRead(member1.getMemberId(), 0);
        assertThat(finalUnreadCount).isEqualTo(0L);
    }
}