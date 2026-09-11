package com.example.soccerplatform.integration.footballdata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FootballDataResponse(List<FootballDataMatch> matches) {
}
