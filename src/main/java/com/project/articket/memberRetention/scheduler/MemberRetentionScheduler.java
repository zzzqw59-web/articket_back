package com.project.articket.memberRetention.scheduler;

import com.project.articket.memberRetention.service.MemberRetentionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberRetentionScheduler {

    private final MemberRetentionService memberRetentionService;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    public void deleteExpiredRetentions() {

        memberRetentionService.deleteExpiredRetentions();
    }
}