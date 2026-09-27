package com.project.articket.verification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "VERIFICATION")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Verification {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "VERIFICATION_SEQ_GEN"
    )
    @SequenceGenerator(
            name = "VERIFICATION_SEQ_GEN",
            sequenceName = "VERIFICATION_SEQ",
            allocationSize = 1
    )
    @Column(name = "VERIFICATION_ID")
    private Long verificationId;

    @Column(name = "PHONE_NUMBER", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "VERIFICATION_CODE", nullable = false, length = 10)
    private String verificationCode;

    @Column(name = "VERIFICATION_TYPE", nullable = false, length = 20)
    private String verificationType;

    @Column(
            name = "VERIFICATION_CREATED_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime verificationCreatedAt;

    @Column(
            name = "VERIFICATION_EXPIRES_AT",
            nullable = false,
            columnDefinition = "DATE"
    )
    private LocalDateTime verificationExpiresAt;

    @Column(
            name = "VERIFICATION_VERIFIED_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime verificationVerifiedAt;

    @Column(
            name = "VERIFICATION_USED_AT",
            columnDefinition = "DATE"
    )
    private LocalDateTime verificationUsedAt;

    public void verify(LocalDateTime verifiedAt) {
        this.verificationVerifiedAt = verifiedAt;
    }
}