package com.project.articket.review.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import com.project.articket.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public PageResponseDTO<ReviewDTO> reviewList(@RequestParam(required = false) String keyword, PageRequestDTO pageRequestDTO) {
        if (keyword == null || keyword.isBlank()) {
            PageResponseDTO<ReviewDTO> dto = reviewService.reviewPage(pageRequestDTO);
            return dto;
        } else {
            PageResponseDTO<ReviewDTO> dto = reviewService.reviewSearch(keyword, pageRequestDTO);
            return dto;
        }
    }

    @PostMapping(value = "/reviews", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void reviewCreate(
            Authentication authentication,
            @Valid @ModelAttribute ReviewCreateDTO reviewCreateDTO
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        reviewService.reviewCreate(memberId, reviewCreateDTO);
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
}