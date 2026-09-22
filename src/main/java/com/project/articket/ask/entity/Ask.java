package com.project.articket.ask.entity;

import com.project.articket.askImage.entity.AskImage;
import com.project.articket.askReply.entity.AskReply;
import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ASK")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class Ask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ASK_ID")
    private Long askId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXHIBITION_ID")
    private Exhibition exhibitionId;

    @Column(name = "ASK_TYPE", nullable = false)
    private Integer askType;

    @Column(name = "ASK_SECRET", nullable = false)
    private Integer askSecret;

    @Column(name = "ASK_TITLE", length = 50, nullable = false)
    private String askTitle;

    @Lob
    @Column(name = "ASK_BODY", nullable = false)
    private String askBody;

    @Column(name = "ASK_HITS", nullable = false)
    @Builder.Default
    private Long askHits = 0L;

    @CreationTimestamp
    @Column(name = "ASK_CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime askCreatedAt;

    @UpdateTimestamp
    @Column(name = "ASK_MODIFIED_AT", nullable = false)
    private LocalDateTime askModifiedAt;

    // 연관 관계 양방향 매핑 (JPA Cascade 삭제용)
    @OneToMany(mappedBy = "askId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<AskImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "askId", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @Builder.Default
    private List<AskReply> replies = new ArrayList<>();
}