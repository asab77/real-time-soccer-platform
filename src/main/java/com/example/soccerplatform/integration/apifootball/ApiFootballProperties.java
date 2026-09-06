package com.example.soccerplatform.integration.apifootball;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "api-football")
public record ApiFootballProperties(
        String baseUrl,
        String key,
        List<ConfiguredLeague> leagues
) {
    public record ConfiguredLeague(Long externalId, String name) {
    }
}
