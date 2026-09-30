package com.project.articket.withdraw.repository;

import com.project.articket.withdraw.entity.Withdraw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawRepository
        extends JpaRepository<Withdraw, Long> {
}