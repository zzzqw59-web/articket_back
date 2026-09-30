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
    public void reviewCreate(@Valid @ModelAttribute ReviewCreateDTO reviewCreateDTO) {
//        JWT에서 로그인 회원의 memberId를 가져온 뒤
        reviewService.reviewCreate(1L, reviewCreateDTO);
    }

    @PutMapping(value = "/reviews/{reviewId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void reviewUpdate(@PathVariable Long reviewId, @Valid @ModelAttribute ReviewUpdateDTO reviewUpdateDTO) {
        reviewService.reviewUpdate(reviewId, reviewUpdateDTO);
    }

    @DeleteMapping("/reviews/{reviewId}")
    public void reviewDelete(@PathVariable Long reviewId) {
        reviewService.reviewDelete(reviewId);
    }

    @GetMapping("/reviews/{reviewId}")
    public ReviewDTO reviewDetail(@PathVariable Long reviewId) {
        ReviewDTO dto = reviewService.reviewDetail(reviewId);
        return dto;
    }
}
