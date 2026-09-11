package com.example.soccerplatform.controller;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.service.FixtureSyncService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class FixtureSyncController {

    private final FixtureSyncService fixtureSyncService;

    public FixtureSyncController(FixtureSyncService fixtureSyncService) {
        this.fixtureSyncService = fixtureSyncService;
    }

    @PostMapping("/internal/sync/fixtures")
    public FixtureSyncSummary synchronizeFixtures(
            @RequestParam Long leagueId,
            @RequestParam LocalDate date
    ) {
        return fixtureSyncService.synchronize(leagueId, date);
    }
}
