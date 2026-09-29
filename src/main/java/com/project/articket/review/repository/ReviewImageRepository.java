package com.project.articket.review.repository;

import com.project.articket.review.entity.ReviewImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImage, Long> {
    List<ReviewImage> findByReviewReviewIdOrderByReviewImageOrder(Long reviewId);
}
