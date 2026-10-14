package com.project.articket.verification.service;

import com.project.articket.verification.entity.Verification;
import com.project.articket.verification.repository.VerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final SolapiService solapiService;
    private final PlatformTransactionManager transactionManager;

    private final SecureRandom secureRandom = new SecureRandom();

    private static final int SEND_INTERVAL_SECONDS = 60;
    private static final int MAX_SEND_PER_HOUR = 5;
    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private final Object[] verificationLocks =
            IntStream.range(0, 64).mapToObj(index -> new Object()).toArray();

    private final ConcurrentHashMap<Long, FailedAttempt> failedAttempts =
            new ConcurrentHashMap<>();

    private final AtomicLong lastCleanupAt = new AtomicLong(0);

    private record FailedAttempt(int count, LocalDateTime expiresAt) {
    }

    public void sendVerificationCode(
            String phoneNumber,
            String verificationType
    ) {

        validateVerificationRequest(phoneNumber, verificationType);

        synchronized (verificationLocks[Math.floorMod(phoneNumber.hashCode(), verificationLocks.length)]) {

            LocalDateTime now = LocalDateTime.now()
                    .withNano(0);

            Verification latestVerification = verificationRepository
                    .findTopByPhoneNumberOrderByVerificationCreatedAtDescVerificationIdDesc(
                            phoneNumber
                    )
                    .orElse(null);

            if (latestVerification != null
                    && latestVerification.getVerificationCreatedAt()
                    .isAfter(now.minusSeconds(SEND_INTERVAL_SECONDS))) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "인증번호는 60초 후에 다시 요청할 수 있습니다."
                );
            }

            long sendCount = verificationRepository
                    .countByPhoneNumberAndVerificationCreatedAtAfter(
                            phoneNumber,
                            now.minusHours(1)
                    );

            if (sendCount >= MAX_SEND_PER_HOUR) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "인증번호는 한 시간에 최대 5회까지 요청할 수 있습니다."
                );
            }

            String verificationCode =
                    String.format("%06d", secureRandom.nextInt(1000000));

            LocalDateTime expiresAt = now.plusMinutes(5);

            Verification verification = Verification.builder()
                    .phoneNumber(phoneNumber)
                    .verificationCode(verificationCode)
                    .verificationType(verificationType)
                    .verificationExpiresAt(expiresAt)
                    .build();

            TransactionTemplate transactionTemplate =
                    new TransactionTemplate(transactionManager);

            transactionTemplate.executeWithoutResult(status -> {

                verificationRepository.saveAndFlush(verification);

                try {
                    solapiService.sendVerificationCode(
                            phoneNumber,
                            verificationCode
                    );

                } catch (RuntimeException exception) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_GATEWAY,
                            "인증번호 문자 발송에 실패했습니다. 잠시 후 다시 시도해 주세요.",
                            exception
                    );
                }
            });
        }
    }

    public void verifyVerificationCode(
            String phoneNumber,
            String verificationCode,
            String verificationType
    ) {

        validateVerificationRequest(phoneNumber, verificationType);

        synchronized (verificationLocks[Math.floorMod(phoneNumber.hashCode(), verificationLocks.length)]) {

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

            cleanupExpiredFailedAttempts(now);

            if (now.isAfter(verification.getVerificationExpiresAt())) {
                failedAttempts.remove(verification.getVerificationId());
                throw new RuntimeException("인증번호가 만료되었습니다.");
            }

            FailedAttempt attempt = failedAttempts.get(verification.getVerificationId());
            int failedCount = attempt == null ? 0 : attempt.count();

            if (failedCount >= MAX_VERIFY_ATTEMPTS) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "인증번호 입력 횟수를 초과했습니다. 새 인증번호를 요청해 주세요."
                );
            }

            if (!verification.getVerificationCode().equals(verificationCode)) {
                failedAttempts.put(
                        verification.getVerificationId(),
                        new FailedAttempt(failedCount + 1, verification.getVerificationExpiresAt())
                );

                if (failedCount + 1 >= MAX_VERIFY_ATTEMPTS) {
                    throw new ResponseStatusException(
                            HttpStatus.TOO_MANY_REQUESTS,
                            "인증번호 입력 횟수를 초과했습니다. 새 인증번호를 요청해 주세요."
                    );
                }

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "인증번호가 일치하지 않습니다."
                );
            }

            verification.verify(now);
            verificationRepository.saveAndFlush(verification);
            failedAttempts.remove(verification.getVerificationId());
        }
    }

    private void validateVerificationRequest(
            String phoneNumber,
            String verificationType
    ) {

        if (phoneNumber == null || !phoneNumber.matches("01[0-9]{8,9}")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "올바른 휴대폰 번호를 입력해 주세요."
            );
        }

        if (!"SIGNUP".equals(verificationType)
                && !"PASSWORD_RESET".equals(verificationType)
                && !"MEMBER_UPDATE".equals(verificationType)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "올바르지 않은 인증 목적입니다."
            );
        }
    }

    private void cleanupExpiredFailedAttempts(LocalDateTime now) {

        long currentTime = System.currentTimeMillis();
        long previousTime = lastCleanupAt.get();

        if (currentTime - previousTime < 60000
                || !lastCleanupAt.compareAndSet(previousTime, currentTime)) {
            return;
        }

        failedAttempts.entrySet().removeIf(
                entry -> now.isAfter(entry.getValue().expiresAt())
        );
    }

    @Transactional
    public void useVerifiedVerification(
            String phoneNumber,
            String verificationType
    ) {

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