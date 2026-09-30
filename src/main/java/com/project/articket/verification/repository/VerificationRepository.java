package com.project.articket.verification.repository;

import com.project.articket.verification.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository
        extends JpaRepository<Verification, Long> {

    Optional<Verification>
    findTopByPhoneNumberAndVerificationTypeOrderByVerificationCreatedAtDescVerificationIdDesc(
            String phoneNumber,
            String verificationType
    );
}