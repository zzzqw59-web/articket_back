package com.project.articket.askReply.controller;

import com.project.articket.askReply.dto.AskReplyDTO;
import com.project.articket.askReply.dto.AskReplyRequestDTO;
import com.project.articket.askReply.service.AskReplyService;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AskReplyController {

    private final AskReplyService askReplyService;

    // ASK-COM-001: 문의 댓글 등록
    // POST /api/asks/{askId}/replies
    @PostMapping("/api/asks/{askId}/replies")
    public ResponseEntity<Long> createReply(
            @PathVariable("askId") Long askId,
            Authentication authentication,
            @Valid @RequestBody AskReplyRequestDTO requestDto
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        Long replyId =
                askReplyService.createReply(
                        askId,
                        memberId,
                        requestDto
                );

        return ResponseEntity.ok(
                replyId
        );
    }

    // ASK-COM-002: 특정 문의글의 댓글 목록 조회
    // GET /api/asks/{askId}/replies
    @GetMapping("/api/asks/{askId}/replies")
    public ResponseEntity<PageResponseDTO<AskReplyDTO>> getReplyList(
            @PathVariable("askId") Long askId,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            Authentication authentication
    ) {

        Long loginMemberId =
                getMemberId(
                        authentication
                );

        PageResponseDTO<AskReplyDTO> response =
                askReplyService.getReplyList(
                        askId,
                        loginMemberId,
                        pageRequestDTO
                );

        return ResponseEntity.ok(
                response
        );
    }

    // ASK-COM-003: 문의 댓글 수정
    // PUT /api/asks/{askId}/replies/{replyId}
    @PutMapping("/api/asks/{askId}/replies/{replyId}")
    public ResponseEntity<Void> updateReply(
            @PathVariable("replyId") Long replyId,
            Authentication authentication,
            @RequestBody AskReplyRequestDTO requestDto
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        askReplyService.updateReply(
                replyId,
                memberId,
                requestDto
        );

        return ResponseEntity.ok()
                .build();
    }

    // ASK-COM-004: 문의 댓글 삭제
    // DELETE /api/asks/{askId}/replies/{replyId}
    @DeleteMapping("/api/asks/{askId}/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable("askId") Long askId,
            @PathVariable("replyId") Long replyId,
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        askReplyService.deleteReply(
                replyId,
                memberId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // ASK-COM-005 (마이페이지): 내가 작성한 댓글 목록 페이징 조회
    // GET /api/my/replies
    @GetMapping("/api/my/replies")
    public ResponseEntity<PageResponseDTO<AskReplyDTO>> getMyReplies(
            Authentication authentication,
            PageRequestDTO pageRequestDTO
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        PageResponseDTO<AskReplyDTO> response =
                askReplyService
                        .getMyReplyList(
                                memberId,
                                pageRequestDTO
                        );

        return ResponseEntity.ok(
                response
        );
    }

    private Long getMemberId(
            Authentication authentication
    ) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return null;
        }

        Object principal =
                authentication.getPrincipal();

        if (!(principal instanceof Long)) {
            return null;
        }

        return (Long) principal;
    }
}