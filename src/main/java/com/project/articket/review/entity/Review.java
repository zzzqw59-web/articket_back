package com.project.articket.review.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "REVIEW")
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_ID", nullable = false)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXHIBITION_ID", nullable = false)
    private Exhibition exhibition;

    @Column(name = "REVIEW_TITLE", nullable = false, length = 100)
    private String reviewTitle;

    @Lob
    @Column(name = "REVIEW_BODY", nullable = false)
    private String reviewBody;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime reviewCreatedAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime reviewModifiedAt;

    @Column(name = "REVIEW_HITS", nullable = false)
    private Integer reviewHits = 0;
}
