package com.project.articket.verification;

import com.project.articket.verification.entity.Verification;
import com.project.articket.verification.repository.VerificationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class VerificationRepositoryTests {

    @Autowired
    private VerificationRepository verificationRepository;

    @Test
    void verificationInsertTest() {

        LocalDateTime createdAt = LocalDateTime.now()
                .withSecond(0)
                .withNano(0);

        LocalDateTime expiresAt = createdAt.plusMinutes(5);

        Verification verification = Verification.builder()
                .phoneNumber("01011112222")
                .verificationCode("123456")
                .verificationType("SIGNUP")
                .verificationCreatedAt(createdAt)
                .verificationExpiresAt(expiresAt)
                .build();

        Verification savedVerification =
                verificationRepository.save(verification);

        assertNotNull(savedVerification.getVerificationId());

        Verification foundVerification = verificationRepository
                .findById(savedVerification.getVerificationId())
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
                createdAt,
                foundVerification.getVerificationCreatedAt()
        );

        assertEquals(
                expiresAt,
                foundVerification.getVerificationExpiresAt()
        );

        assertNull(foundVerification.getVerificationVerifiedAt());
        assertNull(foundVerification.getVerificationUsedAt());
    }
}