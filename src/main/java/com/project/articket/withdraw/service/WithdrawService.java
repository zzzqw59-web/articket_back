package com.project.articket.withdraw.service;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.member.service.MemberService;
import com.project.articket.member.service.RefreshTokenService;
import com.project.articket.memberRetention.service.MemberRetentionService;
import com.project.articket.notification.service.NotificationService;
import com.project.articket.payment.entity.PaymentStatus;
import com.project.articket.reservation.entity.ReservationStatus;
import com.project.articket.wish.service.WishService;
import com.project.articket.withdraw.dto.WithdrawRequestDTO;
import com.project.articket.withdraw.dto.WithdrawResponseDTO;
import com.project.articket.withdraw.entity.Withdraw;
import com.project.articket.withdraw.enums.WithdrawStatus;
import com.project.articket.withdraw.repository.WithdrawPaymentRepository;
import com.project.articket.withdraw.repository.WithdrawRepository;
import com.project.articket.withdraw.repository.WithdrawReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WithdrawService {

    private final WithdrawRepository withdrawRepository;
    private final WithdrawReservationRepository withdrawReservationRepository;
    private final WithdrawPaymentRepository withdrawPaymentRepository;
    private final MemberRepository memberRepository;
    private final MemberService memberService;
    private final MemberRetentionService memberRetentionService;
    private final PasswordEncoder passwordEncoder;
    private final WishService wishService;
    private final NotificationService notificationService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public void requestWithdraw(
            Long memberId,
            WithdrawRequestDTO requestDTO
    ) {
        Member member =
                memberRepository.findById(memberId)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원 정보를 찾을 수 없습니다."
                                )
                        );

        if (!passwordEncoder.matches(
                requestDTO.getPassword(),
                member.getMemberPassword()
        )) {
            throw new RuntimeException(
                    "비밀번호가 일치하지 않습니다."
            );
        }

        if (Member.TYPE_ADMIN.equals(
                member.getMemberType()
        )) {
            throw new RuntimeException(
                    "관리자 계정은 회원 탈퇴를 신청할 수 없습니다."
            );
        }

        boolean exists =
                withdrawRepository
                        .existsByMemberMemberIdAndWithdrawStatus(
                                memberId,
                                WithdrawStatus.IN_PROGRESS
                        );

        if (exists) {
            throw new RuntimeException(
                    "이미 탈퇴 신청이 진행 중입니다."
            );
        }

        boolean hasActiveReservation =
                withdrawReservationRepository
                        .existsByMemberMemberIdAndReservationStatusIn(
                                memberId,
                                List.of(
                                        ReservationStatus.PENDING,
                                        ReservationStatus.RESERVED
                                )
                        );

        if (hasActiveReservation) {
            throw new RuntimeException(
                    "진행 중이거나 완료된 예약이 있어 탈퇴를 신청할 수 없습니다."
            );
        }

        boolean hasActivePayment =
                withdrawPaymentRepository
                        .existsByReservation_Member_MemberIdAndPaymentStatusIn(
                                memberId,
                                List.of(
                                        PaymentStatus.READY,
                                        PaymentStatus.DONE
                                )
                        );

        if (hasActivePayment) {
            throw new RuntimeException(
                    "처리 중이거나 완료된 결제가 있어 탈퇴를 신청할 수 없습니다."
            );
        }

        LocalDateTime now =
                LocalDateTime.now().withNano(0);

        Withdraw withdraw =
                Withdraw.builder()
                        .member(member)
                        .withdrawStatus(
                                WithdrawStatus.IN_PROGRESS
                        )
                        .withdrawDue(
                                now.plusDays(30)
                        )
                        .build();

        withdrawRepository.save(withdraw);
    }

    @Transactional
    public void cancelWithdraw(Long memberId) {

        Withdraw withdraw =
                withdrawRepository
                        .findByMemberMemberIdAndWithdrawStatus(
                                memberId,
                                WithdrawStatus.IN_PROGRESS
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "진행 중인 탈퇴 신청이 없습니다."
                                )
                        );

        LocalDate today =
                LocalDate.now(
                        ZoneId.of("Asia/Seoul")
                );

        LocalDate withdrawDueDate =
                withdraw.getWithdrawDue()
                        .toLocalDate();

        if (!today.isBefore(withdrawDueDate)) {
            throw new RuntimeException(
                    "탈퇴 유예기간이 종료되어 취소할 수 없습니다."
            );
        }

        withdraw.cancel();
    }

    @Transactional(readOnly = true)
    public WithdrawResponseDTO getWithdrawStatus(Long memberId) {

        Withdraw withdraw =
                withdrawRepository
                        .findTopByMemberMemberIdOrderByWithdrawIdDesc(
                                memberId
                        )
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "회원탈퇴 신청 이력이 없습니다."
                                )
                        );

        return WithdrawResponseDTO.builder()
                .withdrawStatus(
                        withdraw.getWithdrawStatus()
                )
                .withdrawCreatedAt(
                        withdraw.getWithdrawCreatedAt()
                )
                .withdrawCanceledAt(
                        withdraw.getWithdrawCanceledAt()
                )
                .withdrawnAt(
                        withdraw.getWithdrawnAt()
                )
                .withdrawDue(
                        withdraw.getWithdrawDue()
                )
                .build();
    }

    @Transactional
    public void completeExpiredWithdraws() {

        List<Long> expiredWithdrawIds =
                withdrawRepository.findExpiredWithdraws()
                        .stream()
                        .map(Withdraw::getWithdrawId)
                        .toList();

        for (Long withdrawId : expiredWithdrawIds) {

            Withdraw targetWithdraw =
                    withdrawRepository.findById(withdrawId)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "탈퇴 신청 정보를 찾을 수 없습니다."
                                    )
                            );

            Long memberId =
                    targetWithdraw.getMember()
                            .getMemberId();

            boolean hasPaymentHistory =
                    withdrawPaymentRepository
                            .existsByReservation_Member_MemberId(
                                    memberId
                            );

            deleteWithdrawnMemberRelatedData(memberId);

            Withdraw managedWithdraw =
                    withdrawRepository.findById(withdrawId)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "탈퇴 신청 정보를 찾을 수 없습니다."
                                    )
                            );

            Member member =
                    memberRepository.findById(memberId)
                            .orElseThrow(
                                    () -> new RuntimeException(
                                            "회원 정보를 찾을 수 없습니다."
                                    )
                            );

            managedWithdraw.complete();

            if (hasPaymentHistory) {
                memberRetentionService.saveRetention(
                        member,
                        managedWithdraw.getWithdrawnAt()
                );
            }

            memberService.anonymizeWithdrawnMember(memberId);
        }
    }

    @Transactional
    public void deleteWithdrawnMemberRelatedData(
            Long memberId
    ) {

        notificationService.deleteAllNotifications(memberId);

        wishService.deleteAllWishes(memberId);

        refreshTokenService.delete(memberId);
    }
}