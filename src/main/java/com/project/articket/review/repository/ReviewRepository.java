package com.project.articket.review.repository;

import com.project.articket.review.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 내 리뷰 목록
    Page<Review> findByMemberMemberId(Long memberId, Pageable pageable);

    Page<Review> findByReviewTitleContainingOrReviewBodyContaining(String reviewTitle, String reviewBody, Pageable pageable);

    Page<Review> findByReviewTitleContaining(String keyword, Pageable pageable);

    Page<Review> findByMemberMemberNicknameContaining(String keyword, Pageable pageable);

    Page<Review> findByExhibitionExhibitionTitleContaining(String keyword, Pageable pageable);

    Page<Review> findByReviewTitleContainingOrReviewBodyContainingOrMemberMemberNicknameContainingOrExhibitionExhibitionTitleContaining(
            String reviewTitle,
            String reviewBody,
            String memberNickname,
            String exhibitionTitle,
            Pageable pageable
    );

    boolean existsByMemberMemberIdAndExhibitionExhibitionId(Long memberId, Long exhibitionId);
}
