package com.example.soccerplatform.dto;

public record FixtureSyncSummary(
        int fixturesReceived,
        int matchesCreated,
        int matchesUpdated
) {
}
