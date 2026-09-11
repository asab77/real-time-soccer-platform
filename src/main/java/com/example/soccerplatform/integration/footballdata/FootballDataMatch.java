package com.example.soccerplatform.integration.footballdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FootballDataMatch(
        Long id,
        OffsetDateTime utcDate,
        String status,
        Team homeTeam,
        Team awayTeam,
        Score score
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Score(FullTime fullTime) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FullTime(Integer home, Integer away) {
    }
}
