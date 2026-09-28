package com.project.articket.notification.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.notification.dto.NotificationCreateDTO;
import com.project.articket.notification.dto.NotificationDTO;
import com.project.articket.notification.entity.Notification;
import com.project.articket.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    // 1. 알림 생성
    @Override
    @Transactional
    public Long createNotification(NotificationCreateDTO createDto) {
        Notification notification = createDto.toEntity();
        Notification saved = notificationRepository.save(notification);
        return saved.getNotificationId();
    }

    // 2. 내 전체 알림 목록 조회 (페이징)
    @Override
    public PageResponseDTO<NotificationDTO> getMyNotifications(Long memberId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("notificationId");
        Page<Notification> result = notificationRepository.findByMemberId_MemberIdOrderByNotificationIdDesc(memberId, pageable);

        List<NotificationDTO> dtoList = result.getContent().stream()
                .map(NotificationDTO::from)
                .toList();

        return new PageResponseDTO<>(dtoList, pageRequestDTO, result.getTotalElements());
    }

    // 3. 안 읽은 알림 목록 조회
    @Override
    public List<NotificationDTO> getUnreadNotifications(Long memberId) {
        return notificationRepository.findByMemberId_MemberIdAndNotificationIsReadOrderByNotificationIdDesc(memberId, 0)
                .stream()
                .map(NotificationDTO::from)
                .toList();
    }

    // 4. 안 읽은 알림 개수 조회
    @Override
    public long getUnreadCount(Long memberId) {
        return notificationRepository.countByMemberId_MemberIdAndNotificationIsRead(memberId, 0);
    }

    // 5. 단건 읽음 처리
    @Override
    @Transactional
    public void readNotification(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다. notificationId=" + notificationId));

        if (!notification.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("해당 알림을 읽을 권한이 없습니다.");
        }

        notification.read();
    }

    // 6. 전체 일괄 읽음 처리
    @Override
    @Transactional
    public void readAllNotifications(Long memberId) {
        notificationRepository.markAllAsReadByMemberId(memberId, LocalDateTime.now());
    }

    // 7. 단건 알림 삭제
    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long memberId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 알림입니다. notificationId=" + notificationId));

        // 본인의 알림인지 권한 검증
        if (!notification.getMemberId().getMemberId().equals(memberId)) {
            throw new IllegalStateException("해당 알림을 삭제할 권한이 없습니다.");
        }

        notificationRepository.delete(notification);
    }

    // 8. 전체 일괄 삭제
    @Override
    @Transactional
    public void deleteAllNotifications(Long memberId) {
        notificationRepository.deleteAllByMemberId(memberId);
    }
}