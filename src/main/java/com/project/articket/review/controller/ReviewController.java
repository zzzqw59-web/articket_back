package com.project.articket.review.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.*;
import com.project.articket.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public PageResponseDTO<ReviewDTO> reviewList(@RequestParam(required = false) String searchType, @RequestParam(required = false) String keyword, PageRequestDTO pageRequestDTO) {
        if (keyword == null || keyword.isBlank()) {
            PageResponseDTO<ReviewDTO> dto = reviewService.reviewPage(pageRequestDTO);
            return dto;
        } else {
            PageResponseDTO<ReviewDTO> dto = reviewService.reviewSearch(searchType, keyword, pageRequestDTO);
            return dto;
        }
    }

    @GetMapping("/reviews/hits")
    public PageResponseDTO<ReviewDTO> reviewHitsList(
            PageRequestDTO pageRequestDTO
    ) {
        return reviewService.reviewHitsPage(pageRequestDTO);
    }

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void reviewCreate(
            Authentication authentication,
            @Valid @ModelAttribute ReviewCreateDTO reviewCreateDTO
    ) {

        Long memberId = (Long) authentication.getPrincipal();

        reviewService.reviewCreate(memberId, reviewCreateDTO);
    }

    @GetMapping("/reviews/my-exhibitions")
    public ResponseEntity<List<ReviewAvailableExhibitionDTO>> getAvailableExhibitionsForReview(Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        List<ReviewAvailableExhibitionDTO> response = reviewService.getAvailableExhibitionsForReview(memberId);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void reviewUpdate(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewUpdateDTO reviewUpdateDTO, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reviewService.reviewUpdate(memberId, reviewId, reviewUpdateDTO);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void reviewDelete(@PathVariable Long reviewId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reviewService.reviewDelete(memberId, reviewId);
    }

    @GetMapping("/reviews/{reviewId}")
    public ReviewDTO reviewDetail(@PathVariable Long reviewId) {
        ReviewDTO dto = reviewService.reviewDetail(reviewId);
        return dto;
    }

    // 마이페이지 조회용
    // 마이페이지 조회용
    @GetMapping("/reviews/me")
    public ResponseEntity<PageResponseDTO<MyReviewListResponseDTO>> getMyReviews(
            PageRequestDTO pageRequestDTO,
            Authentication authentication
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        // PageRequestDTO 하나로 검색 조건(searchType, keyword) 및 정렬(sort)이 모두 전달됨
        PageResponseDTO<MyReviewListResponseDTO> response =
                reviewService.getMyReviews(memberId, pageRequestDTO);

        return ResponseEntity.ok(response);
    }

}