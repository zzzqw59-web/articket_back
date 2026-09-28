package com.project.articket.review.service;
import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;

public interface ReviewService {
    PageResponseDTO<ReviewDTO> reviewPage(PageRequestDTO pageRequestDTO);

    void reviewCreate(Long memberId, ReviewCreateDTO reviewCreateDTO);

    void reviewUpdate(Long reviewId, ReviewUpdateDTO reviewUpdateDTO);

    void reviewDelete(Long reviewId);
}
