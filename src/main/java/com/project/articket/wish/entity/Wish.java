package com.project.articket.wish.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "WISH")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Wish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "WISH_ID")
    private Long wishId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXHIBITION_ID", nullable = false)
    private Exhibition exhibitionId;
}
