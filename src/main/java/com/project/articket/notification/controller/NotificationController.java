package com.project.articket.notification.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.notification.dto.NotificationDTO;
import com.project.articket.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // NOTI-001 내 알림 목록 조회 (페이징)
    // GET /api/notifications?page=1&size=10
    @GetMapping
    public ResponseEntity<PageResponseDTO<NotificationDTO>> getMyNotifications(
            Authentication authentication,
            @ModelAttribute PageRequestDTO pageRequestDTO
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        PageResponseDTO<NotificationDTO> response =
                notificationService.getMyNotifications(
                        memberId,
                        pageRequestDTO
                );

        return ResponseEntity.ok(response);
    }

    // NOTI-002 단건 알림 읽음 처리
    // PATCH /api/notifications/{notificationId}/read
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> readNotification(
            @PathVariable("notificationId") Long notificationId,
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        notificationService.readNotification(
                notificationId,
                memberId
        );

        return ResponseEntity.ok().build();
    }

    // NOTI-003 전체 알림 일괄 읽음 처리
    // PATCH /api/notifications/read-all
    @PatchMapping("/read-all")
    public ResponseEntity<Void> readAllNotifications(
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        notificationService.readAllNotifications(
                memberId
        );

        return ResponseEntity.ok().build();
    }

    // NOTI-004 단건 알림 삭제
    // DELETE /api/notifications/{notificationId}
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable("notificationId") Long notificationId,
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        notificationService.deleteNotification(
                notificationId,
                memberId
        );

        return ResponseEntity.noContent().build();
    }

    // NOTI-005 전체 알림 일괄 삭제
    // DELETE /api/notifications
    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications(
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        notificationService.deleteAllNotifications(
                memberId
        );

        return ResponseEntity.noContent().build();
    }

    // NOTI-006 안 읽은 알림 목록 조회
    // GET /api/notifications/unread
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        List<NotificationDTO> unreadList =
                notificationService.getUnreadNotifications(
                        memberId
                );

        return ResponseEntity.ok(unreadList);
    }

    // NOTI-007 안 읽은 알림 개수 조회 (배지/N 표시용)
    // GET /api/notifications/unread/count
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication
    ) {
        Long memberId =
                (Long) authentication.getPrincipal();

        long count =
                notificationService.getUnreadCount(
                        memberId
                );

        return ResponseEntity.ok(count);
    }
}