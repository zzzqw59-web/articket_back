package com.project.articket.askReply.controller;

import com.project.articket.askReply.dto.AskReplyDTO;
import com.project.articket.askReply.dto.AskReplyRequestDTO;
import com.project.articket.askReply.service.AskReplyService;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
            @RequestParam("memberId") Long memberId,                 // 추후 @AuthenticationPrincipal로 대체
            @RequestParam("loginMemberType") String loginMemberType, // 작성 권한(ADMIN, MANAGER 등) 검증용
            @RequestBody AskReplyRequestDTO requestDto
    ) {
        Long replyId = askReplyService.createReply(askId, memberId, loginMemberType, requestDto);
        return ResponseEntity.ok(replyId);
    }

    // ASK-COM-002: 특정 문의글의 댓글 목록 조회
    // GET /api/asks/{askId}/replies
    @GetMapping("/api/asks/{askId}/replies")
    public ResponseEntity<PageResponseDTO<AskReplyDTO>> getReplyList(
            @PathVariable("askId") Long askId,
            PageRequestDTO pageRequestDTO
    ) {
        PageResponseDTO<AskReplyDTO> response = askReplyService.getReplyList(askId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }

    // ASK-COM-003: 문의 댓글 수정
    // PUT /api/asks/{askId}/replies/{replyId}
    @PutMapping("/api/asks/{askId}/replies/{replyId}")
    public ResponseEntity<Void> updateReply(
            @PathVariable("askId") Long askId,
            @PathVariable("replyId") Long replyId,
            @RequestParam("memberId") Long memberId, // 작성자 본인 검증용
            @RequestBody AskReplyRequestDTO requestDto
    ) {
        askReplyService.updateReply(replyId, memberId, requestDto);
        return ResponseEntity.ok().build();
    }

    // ASK-COM-004: 문의 댓글 삭제
    // DELETE /api/asks/{askId}/replies/{replyId}
    @DeleteMapping("/api/asks/{askId}/replies/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable("askId") Long askId,
            @PathVariable("replyId") Long replyId,
            @RequestParam("memberId") Long memberId,                 // 작성자 본인 확인용
            @RequestParam("loginMemberType") String loginMemberType // 관리자 삭제 권한 확인용
    ) {
        askReplyService.deleteReply(replyId, memberId, loginMemberType);
        return ResponseEntity.noContent().build();
    }

    // ASK-COM-005 (마이페이지): 내가 작성한 댓글 목록 페이징 조회
    // GET /api/my/replies
    @GetMapping("/api/my/replies")
    public ResponseEntity<PageResponseDTO<AskReplyDTO>> getMyReplies(
            @RequestParam("memberId") Long memberId, // 추후 @AuthenticationPrincipal로 대체
            PageRequestDTO pageRequestDTO
    ) {
        PageResponseDTO<AskReplyDTO> response = askReplyService.getMyReplyList(memberId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }
}