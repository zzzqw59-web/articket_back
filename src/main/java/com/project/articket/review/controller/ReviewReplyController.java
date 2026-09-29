package com.project.articket.review.controller;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewReplyCreateDTO;
import com.project.articket.review.dto.ReviewReplyDTO;
import com.project.articket.review.dto.ReviewReplyUpdateDTO;
import com.project.articket.review.service.ReviewReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public void reviewReplyCreate(@PathVariable Long reviewId, @Valid @RequestBody ReviewReplyCreateDTO reviewReplyCreateDTO) {
        reviewReplyService.reviewReplyCreate(reviewId, reviewReplyCreateDTO);
    }

    @PutMapping("/reviews/{reviewId}/replies/{reviewReplyId}")
    public void reviewReplyUpdate(@PathVariable Long reviewReplyId, @Valid @RequestBody ReviewReplyUpdateDTO reviewReplyUpdateDTO) {
        reviewReplyService.reviewReplyUpdate(reviewReplyId, reviewReplyUpdateDTO);
    }

    @DeleteMapping("/reviews/{reviewId}/replies/{reviewReplyId}")
    public void reviewReplyDelete(@PathVariable Long reviewReplyId) {
        reviewReplyService.reviewReplyDelete(reviewReplyId);
    }
}
