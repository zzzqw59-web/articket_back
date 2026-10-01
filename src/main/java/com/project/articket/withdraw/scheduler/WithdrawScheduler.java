package com.project.articket.withdraw.scheduler;

import com.project.articket.withdraw.service.WithdrawService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WithdrawScheduler {

    private final WithdrawService withdrawService;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    public void completeExpiredWithdraws() {

        withdrawService.completeExpiredWithdraws();
    }
}