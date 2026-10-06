package com.project.articket.common.component;

import com.project.articket.exhibition.service.ExhibitionSyncService;
import com.project.articket.venue.service.VenueSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncScheduler {

    private final VenueSyncService venueSyncService;
    private final ExhibitionSyncService exhibitionSyncService;

    @Scheduled(cron = "0 0 3 * * *")
    public void syncAll() {
        venueSyncService.syncVenues();
        exhibitionSyncService.syncExhibitions();
    }
}
