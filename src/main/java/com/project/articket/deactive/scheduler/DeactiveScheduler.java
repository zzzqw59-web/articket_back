package com.project.articket.deactive.scheduler;

import com.project.articket.deactive.service.DeactiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactiveScheduler {

    private final DeactiveService deactiveService;

    @Scheduled(
            cron = "0 0 0 * * *",
            zone = "Asia/Seoul"
    )
    public void releaseExpiredDeactivations() {

        deactiveService
                .releaseExpiredDeactivations();
    }
}