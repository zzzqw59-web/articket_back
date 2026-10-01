package com.project.articket.withdraw.service;

import com.project.articket.member.entity.Member;
import com.project.articket.member.repository.MemberRepository;
import com.project.articket.withdraw.dto.WithdrawRequestDTO;
import com.project.articket.withdraw.dto.WithdrawResponseDTO;
import com.project.articket.withdraw.entity.Withdraw;
import com.project.articket.withdraw.enums.WithdrawStatus;
import com.project.articket.withdraw.repository.WithdrawRepository;
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
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

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

        List<Withdraw> expiredWithdraws =
                withdrawRepository.findExpiredWithdraws();

        for (Withdraw withdraw : expiredWithdraws) {
            withdraw.complete();
        }
    }
}