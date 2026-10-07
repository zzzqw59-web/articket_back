package com.project.articket.mypage.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.mypage.dto.MyReplyListResponseDTO;
import com.project.articket.mypage.service.MyReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    public PageResponseDTO<MyReplyListResponseDTO> getMyReplies(
            Authentication authentication,
            PageRequestDTO pageRequestDTO
    ) {
        Long memberId = (Long) authentication.getPrincipal();
        return myReplyService.getMyReplies(memberId, pageRequestDTO);
    }
}