package com.example.soccerplatform.integration.footballdata;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "football.data")
public record FootballDataProperties(
        String baseUrl,
        String apiKey,
        List<Competition> competitions
) {
    public record Competition(String code, String name) {
    }
}
