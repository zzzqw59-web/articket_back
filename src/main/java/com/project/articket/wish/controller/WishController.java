package com.project.articket.wish.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.wish.dto.WishListResponseDTO;
import com.project.articket.wish.dto.WishToggleResponseDTO;
import com.project.articket.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    // WISH-001: 위시 토글 (추가 / 취소)
    // TODO: 인증 로직 완료 후 memberId 파라미터를 @AuthenticationPrincipal 로 변경 예정
    @PostMapping("/{exhibitionId}")
    public ResponseEntity<WishToggleResponseDTO> toggleWish(
            @PathVariable Long exhibitionId,
            @RequestParam Long memberId) { // 임시로 쿼리 파라미터로 받음 (예: /api/wishes/1?memberId=1)

        WishToggleResponseDTO response = wishService.toggleWish(memberId, exhibitionId);
        return ResponseEntity.ok(response);
    }

    // WISH-002: 마이페이지 - 내 위시리스트 목록 조회
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<WishListResponseDTO>> getMyWishList(
            @RequestParam Long memberId, // 임시로 memberId 받음
            PageRequestDTO pageRequestDTO) {

        PageResponseDTO<WishListResponseDTO> response = wishService.getWishList(memberId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }

    // WISH-003: 마이페이지 - 종료된 전시 위시 일괄 삭제
    @DeleteMapping("/deleteexpired")
    public ResponseEntity<Integer> deleteExpiredWishes(
            @RequestParam Long memberId) { // 임시로 memberId 받음

        int deletedCount = wishService.deleteExpiredWishes(memberId);
        return ResponseEntity.ok(deletedCount);
    }

    // WISH-004: 마이페이지 - 전시 위시 전체 일괄 삭제
    @DeleteMapping("/deleteall")
    public ResponseEntity<Void> deleteAllWishes(@RequestParam Long memberId) {
        wishService.deleteAllWishes(memberId);
        return ResponseEntity.noContent().build();
    }
}