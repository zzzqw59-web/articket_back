package com.project.articket.wish.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.wish.dto.WishListResponseDTO;
import com.project.articket.wish.dto.WishToggleResponseDTO;
import com.project.articket.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    // WISH-001: 위시 토글 (추가 / 취소)
    @PostMapping("/{exhibitionId}")
    public ResponseEntity<WishToggleResponseDTO> toggleWish(
            @PathVariable Long exhibitionId,
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        WishToggleResponseDTO response =
                wishService.toggleWish(
                        memberId,
                        exhibitionId
                );

        return ResponseEntity.ok(
                response
        );
    }

    // WISH-002: 마이페이지 - 내 위시리스트 목록 조회
    @GetMapping("/me")
    public ResponseEntity<PageResponseDTO<WishListResponseDTO>>
    getMyWishList(
            Authentication authentication,
            PageRequestDTO pageRequestDTO
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        PageResponseDTO<WishListResponseDTO> response =
                wishService.getWishList(
                        memberId,
                        pageRequestDTO
                );

        return ResponseEntity.ok(
                response
        );
    }

    // WISH-003: 마이페이지 - 종료된 전시 위시 일괄 삭제
    @DeleteMapping("/deleteexpired")
    public ResponseEntity<Integer> deleteExpiredWishes(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        int deletedCount =
                wishService.deleteExpiredWishes(
                        memberId
                );

        return ResponseEntity.ok(
                deletedCount
        );
    }

    // WISH-004: 마이페이지 - 전시 위시 전체 일괄 삭제
    @DeleteMapping("/deleteall")
    public ResponseEntity<Void> deleteAllWishes(
            Authentication authentication
    ) {

        Long memberId =
                (Long) authentication
                        .getPrincipal();

        wishService.deleteAllWishes(
                memberId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    // WISH-005: 특정 전시 총 위시 수 + 현재 로그인 회원 찜 여부
    @GetMapping("/count/{exhibitionId}")
    public ResponseEntity<WishToggleResponseDTO> getWishCount(
            @PathVariable Long exhibitionId,
            Authentication authentication
    ) {

        Long memberId = null;

        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal()
                instanceof Long) {

            memberId =
                    (Long) authentication
                            .getPrincipal();
        }

        WishToggleResponseDTO response =
                wishService.getWishCountByExhibition(
                        memberId,
                        exhibitionId
                );

        return ResponseEntity.ok(
                response
        );
    }
}