package com.example.soccerplatform.scheduler;

import com.example.soccerplatform.integration.footballdata.FootballDataProperties;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.service.FixtureSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

@Component
@ConditionalOnProperty(name = "soccer.sync.enabled", havingValue = "true")
public class ScheduledFixtureSync {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledFixtureSync.class);
    private final FixtureSyncService fixtureSyncService;
    private final FootballDataProperties properties;
    private final LeagueRepository leagueRepository;
    private final LocalTime activeStartUtc;
    private final LocalTime activeEndUtc;

    public ScheduledFixtureSync(
            FixtureSyncService fixtureSyncService,
            FootballDataProperties properties,
            LeagueRepository leagueRepository,
            @Value("${soccer.sync.active-start-utc:10:00}") LocalTime activeStartUtc,
            @Value("${soccer.sync.active-end-utc:23:59}") LocalTime activeEndUtc
    ) {
        this.fixtureSyncService = fixtureSyncService;
        this.properties = properties;
        this.leagueRepository = leagueRepository;
        this.activeStartUtc = activeStartUtc;
        this.activeEndUtc = activeEndUtc;
    }

    @Scheduled(fixedDelayString = "${soccer.sync.interval:PT1H}",
            initialDelayString = "${soccer.sync.initial-delay:PT1M}")
    public void synchronizeToday() {
        LocalTime now = LocalTime.now(ZoneOffset.UTC);
        if (now.isBefore(activeStartUtc) || now.isAfter(activeEndUtc)) {
            return;
        }

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        int success = 0;
        logger.info("Scheduled fixture synchronization started for {}", today);
        for (FootballDataProperties.Competition competition : properties.competitions()) {
            try {
                var league = leagueRepository.findByNameIgnoreCase(competition.name());
                if (league.isEmpty()) {
                    logger.warn("Scheduled sync skipped missing league {}", competition.name());
                    continue;
                }
                fixtureSyncService.synchronize(league.get().getId(), today);
                success++;
            } catch (RuntimeException exception) {
                logger.warn("Scheduled sync failed for league {} ({})",
                        competition.code(), exception.getClass().getSimpleName());
            }
        }
        logger.info("Scheduled fixture synchronization finished: successful={}, failed={}",
                success, properties.competitions().size() - success);
    }
}
