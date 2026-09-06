package com.example.soccerplatform.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateLeaguePreferenceRequest(
        @NotNull(message = "leagueId is required")
        @Positive(message = "leagueId must be positive")
        Long leagueId
) {
}
