package com.example.soccerplatform.dto;

import com.example.soccerplatform.entity.MatchStatus;
import java.time.OffsetDateTime;

public record MatchUpdateMessage(Long matchId, Long leagueId,
        Long homeTeamId, String homeTeamName, Long awayTeamId, String awayTeamName,
        OffsetDateTime startTime, Integer homeScore, Integer awayScore,
        MatchStatus status, MatchUpdateType updateType) {
}
