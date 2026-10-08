package com.project.articket.common.util;

import com.project.articket.common.enums.MemberRole;
import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.notification.dto.NotificationCreateDTO;
import com.project.articket.notification.service.NotificationService;
import com.project.articket.staff.entity.Staff;
import com.project.articket.staff.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationManager {

    private final MemberRepository memberRepository;
    private final StaffRepository staffRepository;
    private final NotificationService notificationService;

    // 1. 전체 관리자 대상 알림 (문의글 등록 등)
    public void notifyAllAdmins(int notificationType, Long targetId) {
        List<String> adminTypes = List.of(MemberRole.ADMIN.getKey());
        List<Member> adminMembers = memberRepository.findByMemberTypeIn(adminTypes);

        for (Member admin : adminMembers) {
            send(admin, notificationType, targetId);
        }
    }

    // 2. 단일 회원 대상 알림 (댓글 작성 시 원글 작성자 등)
    public void notifyUser(Member receiver, int notificationType, Long targetId, Long currentMemberId) {
        // 본인이 작성한 댓글/알림은 제외
        if (receiver != null && !receiver.getMemberId().equals(currentMemberId)) {
            send(receiver, notificationType, targetId);
        }
    }

    // 3. 특정 전시 담당자(STAFF) 대상 알림
    public void notifyExhibitionStaffs(Long exhibitionId, int notificationType, Long targetId) {
        if (exhibitionId == null) return;

        List<Staff> staffList = staffRepository.findByExhibitionExhibitionId(exhibitionId);

        for (Staff staff : staffList) {
            if (staff.getMember() != null) {
                notificationService.createNotification(
                        NotificationCreateDTO.builder()
                                .receiver(staff.getMember())
                                .notificationType(notificationType)
                                .notificationTargetId(targetId)
                                .build()
                );
            }
        }
    }

    // 공통 단건 발송 메서드
    private void send(Member receiver, int notificationType, Long targetId) {
        notificationService.createNotification(
                NotificationCreateDTO.builder()
                        .receiver(receiver)
                        .notificationType(notificationType)
                        .notificationTargetId(targetId)
                        .build()
        );
    }
}
