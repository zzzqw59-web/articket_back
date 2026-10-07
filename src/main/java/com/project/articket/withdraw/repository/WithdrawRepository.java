package com.project.articket.withdraw.repository;

import com.project.articket.withdraw.entity.Withdraw;
import com.project.articket.withdraw.enums.WithdrawStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface WithdrawRepository
        extends JpaRepository<Withdraw, Long> {

    boolean existsByMemberMemberIdAndWithdrawStatus(
            Long memberId,
            WithdrawStatus withdrawStatus
    );

    Optional<Withdraw> findByMemberMemberIdAndWithdrawStatus(
            Long memberId,
            WithdrawStatus withdrawStatus
    );

    Optional<Withdraw> findTopByMemberMemberIdOrderByWithdrawIdDesc(
            Long memberId
    );

    @Query(
            value = """
                    SELECT *
                    FROM WITHDRAW
                    WHERE WITHDRAW_STATUS = 'IN_PROGRESS'
                      AND TRUNC(WITHDRAW_DUE) <= TRUNC(SYSDATE)
                    """,
            nativeQuery = true
    )
    List<Withdraw> findExpiredWithdraws();
}