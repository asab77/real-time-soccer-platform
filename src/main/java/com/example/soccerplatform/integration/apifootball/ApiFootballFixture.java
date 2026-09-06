package com.example.soccerplatform.integration.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFootballFixture(
        Fixture fixture,
        League league,
        Teams teams,
        Goals goals
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Fixture(Long id, OffsetDateTime date, Status status) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status(@JsonProperty("short") String shortCode) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record League(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Teams(Team home, Team away) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Team(Long id, String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Goals(Integer home, Integer away) {
    }
}
