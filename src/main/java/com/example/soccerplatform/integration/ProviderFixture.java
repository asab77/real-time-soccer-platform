package com.example.soccerplatform.integration;

import com.example.soccerplatform.entity.MatchStatus;

import java.time.OffsetDateTime;

public record ProviderFixture(
        Long externalId,
        OffsetDateTime startTime,
        MatchStatus status,
        ProviderTeam homeTeam,
        ProviderTeam awayTeam,
        Integer homeScore,
        Integer awayScore
) {
    public record ProviderTeam(Long externalId, String name) {
    }
}
