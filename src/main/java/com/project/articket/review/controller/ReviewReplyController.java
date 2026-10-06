package com.project.articket.review.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewReplyCreateDTO;
import com.project.articket.review.dto.ReviewReplyDTO;
import com.project.articket.review.dto.ReviewReplyUpdateDTO;
import com.project.articket.review.service.ReviewReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewReplyController {
    private final ReviewReplyService reviewReplyService;

    @GetMapping("/reviews/{reviewId}/replies")
    public PageResponseDTO<ReviewReplyDTO> reviewReplyList(@PathVariable Long reviewId, PageRequestDTO pageRequestDTO) {
        PageResponseDTO<ReviewReplyDTO> dto = reviewReplyService.reviewReplyPage(reviewId, pageRequestDTO);
        return dto;
    }

    @PostMapping("/reviews/{reviewId}/replies")
    public void reviewReplyCreate(
            @PathVariable Long reviewId,
            Authentication authentication,
            @Valid @RequestBody ReviewReplyCreateDTO reviewReplyCreateDTO
    ) {
        Long memberId = (Long) authentication.getPrincipal();

        reviewReplyService.reviewReplyCreate(reviewId, memberId, reviewReplyCreateDTO);
    }

    @PutMapping("/reviews/{reviewId}/replies/{reviewReplyId}")
    public void reviewReplyUpdate(@PathVariable Long reviewReplyId, Authentication authentication, @Valid @RequestBody ReviewReplyUpdateDTO reviewReplyUpdateDTO) {
        Long memberId = (Long) authentication.getPrincipal();

        reviewReplyService.reviewReplyUpdate(memberId, reviewReplyId, reviewReplyUpdateDTO
        );
    }

    @DeleteMapping("/reviews/{reviewId}/replies/{reviewReplyId}")
    public void reviewReplyDelete(@PathVariable Long reviewReplyId, Authentication authentication) {
        Long memberId = (Long) authentication.getPrincipal();
        reviewReplyService.reviewReplyDelete(memberId, reviewReplyId);
    }
}