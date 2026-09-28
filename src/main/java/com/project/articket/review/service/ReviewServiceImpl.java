package com.project.articket.review.service;

import com.project.articket.common.dto.PageRequestDTO;
import com.project.articket.common.dto.PageResponseDTO;
import com.project.articket.review.dto.ReviewCreateDTO;
import com.project.articket.review.dto.ReviewDTO;
import com.project.articket.review.dto.ReviewUpdateDTO;
import com.project.articket.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository repository;

    @Override
    public PageResponseDTO<ReviewDTO> reviewPage(PageRequestDTO pageRequestDTO) {
        return null;
    }
    @Override
    public void reviewCreate(ReviewCreateDTO reviewCreateDTO) {


    }
    @Override
    public void reviewUpdate(Long reviewId, ReviewUpdateDTO reviewUpdateDTO) {

    }
    @Override
    public void reviewDelete(Long reviewId) {

    }
}
