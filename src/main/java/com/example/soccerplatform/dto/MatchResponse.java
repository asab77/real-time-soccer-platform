package com.example.soccerplatform.dto;

import com.example.soccerplatform.entity.MatchStatus;

import java.time.OffsetDateTime;

public record MatchResponse(
        Long id,
        Long leagueId,
        String leagueName,
        Long homeTeamId,
        String homeTeamName,
        Long awayTeamId,
        String awayTeamName,
        OffsetDateTime startTime,
        Integer homeScore,
        Integer awayScore,
        MatchStatus status
) {
}
