package com.project.articket.review.repository;

import com.project.articket.review.entity.ReviewReply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Long> {
    Page<ReviewReply> findByMemberMemberId(Long memberId, Pageable pageable);
    Page<ReviewReply> findByReviewReviewId(Long reviewId, Pageable pageable);

    List<ReviewReply> findByReviewReviewId(Long reviewId);
}
