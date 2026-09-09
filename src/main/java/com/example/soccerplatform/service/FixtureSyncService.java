package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.exception.InvalidSyncRequestException;
import com.example.soccerplatform.integration.apifootball.ApiFootballClient;
import com.example.soccerplatform.integration.apifootball.ApiFootballFixture;
import com.example.soccerplatform.integration.apifootball.ApiFootballProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FixtureSyncService {

    private static final Logger logger = LoggerFactory.getLogger(FixtureSyncService.class);

    private final ApiFootballClient apiFootballClient;
    private final ApiFootballProperties properties;
    private final FixturePersistenceService fixturePersistenceService;

    public FixtureSyncService(
            ApiFootballClient apiFootballClient,
            ApiFootballProperties properties,
            FixturePersistenceService fixturePersistenceService
    ) {
        this.apiFootballClient = apiFootballClient;
        this.properties = properties;
        this.fixturePersistenceService = fixturePersistenceService;
    }

    public FixtureSyncSummary synchronize(Long externalLeagueId, int season, LocalDate date) {
        validateRequest(externalLeagueId, season, date);

        List<ApiFootballFixture> fixtures = apiFootballClient.getFixtures(
                externalLeagueId,
                season,
                date
        );

        FixtureSyncSummary summary = fixturePersistenceService.persist(fixtures);
        logger.info(
                "Fixture synchronization completed: received={}, created={}, updated={}",
                summary.fixturesReceived(),
                summary.matchesCreated(),
                summary.matchesUpdated()
        );
        return summary;
    }

    private void validateRequest(Long externalLeagueId, int season, LocalDate date) {
        List<ApiFootballProperties.ConfiguredLeague> configuredLeagues = properties.leagues() == null
                ? List.of()
                : properties.leagues();
        boolean configuredLeague = externalLeagueId != null
                && configuredLeagues.stream()
                .anyMatch(league -> league.externalId().equals(externalLeagueId));

        if (!configuredLeague) {
            throw new InvalidSyncRequestException("League is not configured for synchronization");
        }
        if (season < 2000) {
            throw new InvalidSyncRequestException("Season is invalid");
        }
        if (date == null) {
            throw new InvalidSyncRequestException("Date is required");
        }
    }
}
