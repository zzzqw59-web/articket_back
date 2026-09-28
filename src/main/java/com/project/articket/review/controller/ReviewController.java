package com.project.articket.review.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import com.project.articket.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviews")
    public PageResponseDTO<ReviewDTO> reviewList(PageRequestDTO pageRequestDTO) {
        PageResponseDTO<ReviewDTO> dto = reviewService.reviewPage(pageRequestDTO);
        return dto;
    }

    @PostMapping("/reviews")
    public void reviewCreate(@Valid @RequestBody ReviewCreateDTO reviewCreateDTO) {
//        JWT에서 로그인 회원의 memberId를 가져온 뒤
        reviewService.reviewCreate(1L, reviewCreateDTO);
    }

    @PutMapping("/reviews/{reviewId}")
    public void reviewUpdate(@PathVariable Long reviewId, @Valid @RequestBody ReviewUpdateDTO reviewUpdateDTO) {
        reviewService.reviewUpdate(reviewId, reviewUpdateDTO);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void reviewDelete(@PathVariable Long reviewId) {
        reviewService.reviewDelete(reviewId);
    }
}
