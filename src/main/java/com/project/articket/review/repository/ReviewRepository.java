package com.project.articket.review.repository;

import com.project.articket.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 내 리뷰 목록
    Page<Review> findByMemberMemberId(Long memberId, Pageable pageable);

    Page<Review> findByReviewTitleContainingOrReviewBodyContaining(String reviewTitle, String reviewBody, Pageable pageable);
}
