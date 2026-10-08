
package com.project.articket.verification.service;

import com.project.articket.verification.entity.Verification;
import com.project.articket.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final SolapiService solapiService;

    private final SecureRandom secureRandom = new SecureRandom();

    private void validateVerificationType(String verificationType) {
        if (!List.of(
                "SIGNUP",
                "PASSWORD_RESET",
                "MEMBER_UPDATE"
        ).contains(verificationType)) {
            throw new IllegalArgumentException(
                    "지원하지 않는 휴대폰 인증 목적입니다."
            );
        }
    }

    public void sendVerificationCode(
            String phoneNumber,
            String verificationType
    ) {
        validateVerificationType(verificationType);

        String verificationCode =
                String.format("%06d", secureRandom.nextInt(1000000));

        LocalDateTime expiresAt = LocalDateTime.now()
                .withNano(0)
                .plusMinutes(5);

        Verification verification = Verification.builder()
                .phoneNumber(phoneNumber)
                .verificationCode(verificationCode)
                .verificationType(verificationType)
                .verificationExpiresAt(expiresAt)
                .build();

        verificationRepository.save(verification);

        solapiService.sendVerificationCode(
                phoneNumber,
                verificationCode
        );
    }

    @Transactional
    public void verifyVerificationCode(
            String phoneNumber,
            String verificationCode,
            String verificationType
    ) {
        validateVerificationType(verificationType);

        Verification verification = verificationRepository
                .findTopByPhoneNumberAndVerificationTypeOrderByVerificationCreatedAtDescVerificationIdDesc(
                        phoneNumber,
                        verificationType
                )
                .orElseThrow(() ->
                        new RuntimeException("인증 요청 정보를 찾을 수 없습니다.")
                );

        if (verification.getVerificationUsedAt() != null) {
            throw new RuntimeException("이미 사용된 인증입니다.");
        }

        if (verification.getVerificationVerifiedAt() != null) {
            throw new RuntimeException("이미 인증이 완료되었습니다.");
        }

        LocalDateTime now = LocalDateTime.now()
                .withNano(0);

        if (now.isAfter(verification.getVerificationExpiresAt())) {
            throw new RuntimeException("인증번호가 만료되었습니다.");
        }

        if (!verification.getVerificationCode().equals(verificationCode)) {
            throw new RuntimeException("인증번호가 일치하지 않습니다.");
        }

        verification.verify(now);
    }

    @Transactional
    public void useVerifiedVerification(
            String phoneNumber,
            String verificationType
    ) {
        validateVerificationType(verificationType);

        Verification verification = verificationRepository
                .findTopByPhoneNumberAndVerificationTypeOrderByVerificationCreatedAtDescVerificationIdDesc(
                        phoneNumber,
                        verificationType
                )
                .orElseThrow(() ->
                        new RuntimeException("인증 요청 정보를 찾을 수 없습니다.")
                );

        if (verification.getVerificationVerifiedAt() == null) {
            throw new RuntimeException("휴대폰 인증이 완료되지 않았습니다.");
        }

        if (verification.getVerificationUsedAt() != null) {
            throw new RuntimeException("이미 사용된 인증입니다.");
        }

        LocalDateTime now = LocalDateTime.now()
                .withNano(0);

        LocalDateTime usableUntil =
                verification.getVerificationVerifiedAt().plusMinutes(5);

        if (now.isAfter(usableUntil)) {
            throw new RuntimeException("휴대폰 인증 유효시간이 만료되었습니다.");
        }

        verification.use(now);
    }
}
