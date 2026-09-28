package com.project.articket.wish.entity;

import com.project.articket.exhibition.entity.Exhibition;
import com.project.articket.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE) // 회원 삭제 시 연관 위시리스트 자동 삭제
    private Member memberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXHIBITION_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // 전시 삭제 시 연관 위시리스트 자동 삭제
    private Exhibition exhibitionId;
}