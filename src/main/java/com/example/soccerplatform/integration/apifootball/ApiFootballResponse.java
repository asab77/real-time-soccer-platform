package com.example.soccerplatform.integration.apifootball;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ApiFootballResponse(
        JsonNode errors,
        List<ApiFootballFixture> response
) {
}
