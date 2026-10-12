package com.project.articket.review.repository;

import com.project.articket.review.dto.MyReviewListResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewRepositoryCustom {
    // 마이페이지: 내 리뷰 목록 조회
    Page<MyReviewListResponseDTO> searchMyReviews(Long memberId, String searchType, String keyword, String sort, Pageable pageable);
}