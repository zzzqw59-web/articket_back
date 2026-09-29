package com.project.articket.member.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REFRESH_TOKEN")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "REFRESH_TOKEN_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "REFRESH_TOKEN_SEQ_GEN",
            sequenceName = "REFRESH_TOKEN_SEQ",
            allocationSize = 1
    )
    @Column(name = "REFRESH_TOKEN_ID")
    private Long refreshTokenId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "MEMBER_ID",
            nullable = false,
            unique = true
    )
    private Member member;

    @Column(
            name = "REFRESH_TOKEN_VALUE",
            nullable = false,
            length = 1000
    )
    private String refreshTokenValue;

    @Column(
            name = "REFRESH_TOKEN_EXPIRES_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime refreshTokenExpiresAt;

    @Column(
            name = "REFRESH_TOKEN_CREATED_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime refreshTokenCreatedAt;

    @PrePersist
    public void prePersist() {
        this.refreshTokenCreatedAt = LocalDateTime.now()
                .withSecond(0)
                .withNano(0);
    }

    public void updateToken(
            String refreshTokenValue,
            LocalDateTime refreshTokenExpiresAt
    ) {
        this.refreshTokenValue = refreshTokenValue;
        this.refreshTokenExpiresAt = refreshTokenExpiresAt;
    }
}