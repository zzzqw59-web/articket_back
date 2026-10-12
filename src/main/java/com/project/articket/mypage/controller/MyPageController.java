package com.project.articket.mypage.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import com.project.articket.mypage.dto.MyHistoryDTO; // 💡 관람 내역 DTO 임포트
import com.project.articket.mypage.service.MyReplyService;
import com.project.articket.mypage.service.MyHistoryService; // 💡 관람 내역 서비스 임포트
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyReplyService myReplyService;
    private final MyHistoryService myHistoryService; // 💡 관람 내역 서비스 주입

    /**
     * MYPOST-001: 내 댓글 목록 조회
     * GET /api/mypage/replies
     */
    @GetMapping("/replies")
    public ResponseEntity<PageResponseDTO<MyReplyListResponseDTO>> getMyReplies(
            PageRequestDTO pageRequestDTO,
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        PageResponseDTO<MyReplyListResponseDTO> response =
                myReplyService.getMyReplies(memberId, pageRequestDTO);

        return ResponseEntity.ok(response);
    }

    /**
     * HISTORY-001: 관람 내역 조회 (다녀온 전시)
     * GET /api/mypage/history
     */
    @GetMapping("/history") // 명세서 경로 반영 (컨트롤러 prefix 공통 적용 여부에 따라 조정 필요)
    public ResponseEntity<PageResponseDTO<MyHistoryDTO>> getVisitedExhibitions(
            PageRequestDTO pageRequestDTO,
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        PageResponseDTO<MyHistoryDTO> response =
                myHistoryService.getMyVisitedHistories(memberId, pageRequestDTO);

        return ResponseEntity.ok(response);
    }
}