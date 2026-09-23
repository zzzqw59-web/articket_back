package com.project.articket.common.controller;

import com.project.articket.exhibition.service.ExhibitionSyncService;
import com.project.articket.venue.service.VenueSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/sync")
@RequiredArgsConstructor
public class SyncTestController {

    private final VenueSyncService venueSyncService;
    private final ExhibitionSyncService exhibitionSyncService;

    @PostMapping("/venues")
    public String syncVenues() {
        venueSyncService.syncVenues();
        return "Venue 동기화 완료";
    }

    @PostMapping("/exhibitions")
    public String syncExhibitions() {
        exhibitionSyncService.syncExhibitions();
        return "Exhibition 동기화 완료";
    }

    @GetMapping("/all")
    public String syncAll() {
        System.out.println("========== SYNC ALL 실행 ==========");
        venueSyncService.syncVenues();
        System.out.println("========== VENUE 동기화 완료 ==========");
        exhibitionSyncService.syncExhibitions();
        System.out.println("========== EXHIBITION 동기화 완료 ==========");
        return "전체 동기화 완료";
    }
}
