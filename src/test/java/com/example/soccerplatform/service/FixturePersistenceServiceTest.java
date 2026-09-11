package com.example.soccerplatform.service;

import com.example.soccerplatform.cache.MatchCacheInvalidationEvent;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.event.MatchUpdatedEvent;
import com.example.soccerplatform.integration.ProviderFixture;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FixturePersistenceServiceTest {

    private static final OffsetDateTime START_TIME =
            OffsetDateTime.parse("2026-09-06T15:00:00Z");

    private final TeamRepository teamRepository = mock(TeamRepository.class);
    private final MatchRepository matchRepository = mock(MatchRepository.class);
    private final ApplicationEventPublisher eventPublisher =
            mock(ApplicationEventPublisher.class);
    private final FixturePersistenceService service = new FixturePersistenceService(
            teamRepository, matchRepository, eventPublisher
    );

    private final League league = mock(League.class);
    private final Team homeTeam = mock(Team.class);
    private final Team awayTeam = mock(Team.class);
    private final Match existingMatch = mock(Match.class);

    @BeforeEach
    void setUp() {
        when(league.getId()).thenReturn(1L);
        when(homeTeam.getId()).thenReturn(2L);
        when(homeTeam.getName()).thenReturn("Arsenal FC");
        when(awayTeam.getId()).thenReturn(3L);
        when(awayTeam.getName()).thenReturn("Chelsea FC");
        when(teamRepository.findByExternalId(57L)).thenReturn(Optional.of(homeTeam));
        when(teamRepository.findByExternalId(61L)).thenReturn(Optional.of(awayTeam));
        when(matchRepository.findByExternalId(54321L)).thenReturn(Optional.of(existingMatch));
        when(existingMatch.getStartTime()).thenReturn(START_TIME);
        when(existingMatch.getStatus()).thenReturn(MatchStatus.SCHEDULED);
    }

    @Test
    void identicalMatchProducesNoEvents() {
        service.persist(league, List.of(fixture(MatchStatus.SCHEDULED, null, null)));

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void changedMatchProducesUpdateAndCacheInvalidationEvents() {
        service.persist(league, List.of(fixture(MatchStatus.LIVE, 1, 0)));

        verify(eventPublisher).publishEvent(any(MatchUpdatedEvent.class));
        verify(eventPublisher).publishEvent(any(MatchCacheInvalidationEvent.class));
    }

    private ProviderFixture fixture(
            MatchStatus status,
            Integer homeScore,
            Integer awayScore
    ) {
        return new ProviderFixture(
                54321L,
                START_TIME,
                status,
                new ProviderFixture.ProviderTeam(57L, "Arsenal FC"),
                new ProviderFixture.ProviderTeam(61L, "Chelsea FC"),
                homeScore,
                awayScore
        );
    }
}
