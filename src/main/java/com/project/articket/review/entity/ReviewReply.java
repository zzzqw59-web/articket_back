package com.project.articket.review.entity;

import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVIEW_REPLY")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "REVIEW_REPLY_ID", nullable = false)
    private Long reviewReplyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REVIEW_ID", nullable = false)
    private Review review;

    @Column(name = "REVIEW_REPLY_BODY", nullable = false, length = 1000)
    private String reviewReplyBody;

    @CreationTimestamp
    @Column(name = "REVIEW_REPLY_CREATED_AT")
    private LocalDateTime reviewReplyCreatedAt;

    @UpdateTimestamp
    @Column(name = "REVIEW_REPLY_MODIFIED_AT")
    private LocalDateTime reviewReplyModifiedAt;
}
