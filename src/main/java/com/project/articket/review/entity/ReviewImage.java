package com.project.articket.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "REVIEW_IMAGE")
public class ReviewImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_IMAGE_ID", nullable = false)
    private Long reviewImageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    @Column(name = "REVIEW_IMAGE_ORIGIN", nullable = false)
    private String reviewImageOrigin;

    @Column(name = "REVIEW_IMAGE_FILENAME", nullable = false)
    private String reviewImageFilename;

    @Column(name = "REVIEW_IMAGE_URL", nullable = false)
    private String reviewImageUrl;

    @Column(name = "REVIEW_IMAGE_ORDER", nullable = false)
    private Integer reviewImageOrder;

    @CreationTimestamp
    @Column(name = "REVIEW_IMAGE_CREATED_AT", nullable = false)
    private LocalDateTime reviewImageCreatedAt;

    public ReviewImage(Review review, String reviewImageOrigin, String reviewImageFilename, String reviewImageUrl, Integer reviewImageOrder) {
        this.review = review;
        this.reviewImageOrigin = reviewImageOrigin;
        this.reviewImageFilename = reviewImageFilename;
        this.reviewImageUrl = reviewImageUrl;
        this.reviewImageOrder = reviewImageOrder;
    }
}
