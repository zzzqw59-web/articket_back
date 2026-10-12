package com.project.articket.mypage.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import com.project.articket.mypage.service.MyReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyReplyService myReplyService;

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

        // PageRequestDTO 하나로 검색 조건(searchType, keyword) 및 정렬(sort)이 모두 전달됨
        PageResponseDTO<MyReplyListResponseDTO> response =
                myReplyService.getMyReplies(memberId, pageRequestDTO);

        return ResponseEntity.ok(response);
    }
}