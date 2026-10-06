package com.project.articket.verification;

import com.project.articket.verification.entity.Verification;
import com.project.articket.verification.repository.VerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VerificationRepositoryTests {

    @Autowired
    private VerificationRepository verificationRepository;

    @Test
    void verificationInsertTest() {

        LocalDateTime beforeSave = LocalDateTime.now();

        LocalDateTime expiresAt =
                LocalDateTime.now()
                        .withNano(0)
                        .plusMinutes(5);

        Verification verification = Verification.builder()
                .phoneNumber("01011112222")
                .verificationCode("123456")
                .verificationType("SIGNUP")
                .verificationExpiresAt(expiresAt)
                .build();

        Verification savedVerification =
                verificationRepository.saveAndFlush(verification);

        LocalDateTime afterSave = LocalDateTime.now();

        assertNotNull(
                savedVerification.getVerificationId()
        );

        // DB DEFAULT SYSDATE 생성 확인
        assertNotNull(
                savedVerification.getVerificationCreatedAt()
        );

        assertFalse(
                savedVerification.getVerificationCreatedAt()
                        .isBefore(beforeSave.minusSeconds(5))
        );

        assertFalse(
                savedVerification.getVerificationCreatedAt()
                        .isAfter(afterSave.plusSeconds(5))
        );

        Verification foundVerification =
                verificationRepository
                        .findById(
                                savedVerification.getVerificationId()
                        )
                        .orElseThrow();

        assertEquals(
                "01011112222",
                foundVerification.getPhoneNumber()
        );

        assertEquals(
                "123456",
                foundVerification.getVerificationCode()
        );

        assertEquals(
                "SIGNUP",
                foundVerification.getVerificationType()
        );

        assertEquals(
                savedVerification.getVerificationCreatedAt(),
                foundVerification.getVerificationCreatedAt()
        );

        assertEquals(
                expiresAt,
                foundVerification.getVerificationExpiresAt()
        );

        assertNull(
                foundVerification.getVerificationVerifiedAt()
        );

        assertNull(
                foundVerification.getVerificationUsedAt()
        );

        // 최신 인증 요청 조회 확인
        Optional<Verification> latest =
                verificationRepository
                        .findTopByPhoneNumberAndVerificationTypeOrderByVerificationCreatedAtDescVerificationIdDesc(
                                "01011112222",
                                "SIGNUP"
                        );

        assertTrue(latest.isPresent());

        assertEquals(
                savedVerification.getVerificationId(),
                latest.get().getVerificationId()
        );
    }
}