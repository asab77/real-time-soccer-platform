package com.example.soccerplatform.service;

import com.example.soccerplatform.cache.MatchCacheInvalidationEvent;
import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.dto.MatchUpdateMessage;
import com.example.soccerplatform.dto.MatchUpdateType;
import com.example.soccerplatform.event.MatchUpdatedEvent;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.integration.ProviderFixture;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class FixturePersistenceService {

    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FixturePersistenceService(
            TeamRepository teamRepository,
            MatchRepository matchRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.teamRepository = teamRepository;
        this.matchRepository = matchRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public FixtureSyncSummary persist(League league, List<ProviderFixture> fixtures) {
        int created = 0;
        int updated = 0;
        int meaningfulChanges = 0;

        for (ProviderFixture fixture : fixtures) {
            validateFixture(fixture);

            Team homeTeam = findOrCreateTeam(fixture.homeTeam());
            Team awayTeam = findOrCreateTeam(fixture.awayTeam());
            MatchStatus status = fixture.status();
            Optional<Match> existingMatch = matchRepository.findByExternalId(fixture.externalId());

            Match match;
            MatchUpdateType updateType;
            boolean changed;
            if (existingMatch.isPresent()) {
                match = existingMatch.get();
                changed = !Objects.equals(match.getStartTime(), fixture.startTime())
                        || !Objects.equals(match.getHomeScore(), fixture.homeScore())
                        || !Objects.equals(match.getAwayScore(), fixture.awayScore())
                        || match.getStatus() != status;
                match.setStartTime(fixture.startTime());
                match.setScore(fixture.homeScore(), fixture.awayScore());
                match.setStatus(status);
                updated++;
                updateType = MatchUpdateType.UPDATED;
            } else {
                match = new Match(
                        league,
                        homeTeam,
                        awayTeam,
                        fixture.startTime(),
                        status
                );
                match.setExternalId(fixture.externalId());
                match.setScore(fixture.homeScore(), fixture.awayScore());
                created++;
                changed = true;
                updateType = MatchUpdateType.CREATED;
            }

            matchRepository.save(match);
            if (changed) {
                meaningfulChanges++;
                eventPublisher.publishEvent(new MatchUpdatedEvent(new MatchUpdateMessage(
                        match.getId(), league.getId(), homeTeam.getId(), homeTeam.getName(),
                        awayTeam.getId(), awayTeam.getName(), match.getStartTime(),
                        match.getHomeScore(), match.getAwayScore(), match.getStatus(), updateType
                )));
            }
        }

        org.slf4j.LoggerFactory.getLogger(FixturePersistenceService.class)
                .info("Meaningful match changes detected: {}", meaningfulChanges);

        FixtureSyncSummary summary = new FixtureSyncSummary(fixtures.size(), created, updated);
        if (meaningfulChanges > 0) {
            eventPublisher.publishEvent(MatchCacheInvalidationEvent.allMatchCaches());
        }

        return summary;
    }

    private Team findOrCreateTeam(ProviderFixture.ProviderTeam providerTeam) {
        return teamRepository.findByExternalId(providerTeam.externalId())
                .map(team -> {
                    team.setName(providerTeam.name());
                    return team;
                })
                .orElseGet(() -> {
                    Team team = new Team(providerTeam.name());
                    team.setExternalId(providerTeam.externalId());
                    return teamRepository.save(team);
                });
    }

    private void validateFixture(ProviderFixture fixture) {
        boolean invalid = fixture == null
                || fixture.externalId() == null
                || fixture.startTime() == null
                || fixture.status() == null
                || invalidTeam(fixture.homeTeam())
                || invalidTeam(fixture.awayTeam());

        if (invalid) {
            throw new ProviderIntegrationException("Soccer data provider returned malformed fixture data");
        }
    }

    private boolean invalidTeam(ProviderFixture.ProviderTeam team) {
        return team == null || team.externalId() == null || team.name() == null;
    }
}
