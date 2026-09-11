package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.exception.InvalidSyncRequestException;
import com.example.soccerplatform.exception.ResourceNotFoundException;
import com.example.soccerplatform.integration.ProviderFixture;
import com.example.soccerplatform.integration.SoccerDataProvider;
import com.example.soccerplatform.repository.LeagueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FixtureSyncService {

    private static final Logger logger = LoggerFactory.getLogger(FixtureSyncService.class);

    private final SoccerDataProvider soccerDataProvider;
    private final LeagueRepository leagueRepository;
    private final FixturePersistenceService fixturePersistenceService;

    public FixtureSyncService(
            SoccerDataProvider soccerDataProvider,
            LeagueRepository leagueRepository,
            FixturePersistenceService fixturePersistenceService
    ) {
        this.soccerDataProvider = soccerDataProvider;
        this.leagueRepository = leagueRepository;
        this.fixturePersistenceService = fixturePersistenceService;
    }

    public FixtureSyncSummary synchronize(Long leagueId, LocalDate date) {
        validateRequest(leagueId, date);
        League league = leagueRepository.findById(leagueId)
                .orElseThrow(() -> new ResourceNotFoundException("League not found"));

        List<ProviderFixture> fixtures = soccerDataProvider.getFixtures(
                league.getName(), date, date
        );

        FixtureSyncSummary summary = fixturePersistenceService.persist(league, fixtures);
        logger.info(
                "Fixture synchronization completed: received={}, created={}, updated={}",
                summary.fixturesReceived(),
                summary.matchesCreated(),
                summary.matchesUpdated()
        );
        return summary;
    }

    private void validateRequest(Long leagueId, LocalDate date) {
        if (leagueId == null || leagueId <= 0) {
            throw new InvalidSyncRequestException("League ID is invalid");
        }
        if (date == null) {
            throw new InvalidSyncRequestException("Date is required");
        }
    }
}
