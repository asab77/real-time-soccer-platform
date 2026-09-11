package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.integration.ProviderFixture;
import com.example.soccerplatform.integration.SoccerDataProvider;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class FixtureSyncServiceTest {

    private static final LocalDate DATE = LocalDate.of(2026, 9, 6);

    @Autowired FixtureSyncService fixtureSyncService;
    @Autowired LeagueRepository leagueRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired MatchRepository matchRepository;

    @MockitoBean SoccerDataProvider soccerDataProvider;

    private League premierLeague;

    @BeforeEach
    void setUp() {
        premierLeague = leagueRepository.save(new League("Premier League"));
    }

    @Test
    void createsMatchFromProviderFixture() {
        when(soccerDataProvider.getFixtures("Premier League", DATE, DATE))
                .thenReturn(List.of(fixture(MatchStatus.SCHEDULED, null, null)));

        FixtureSyncSummary summary = fixtureSyncService.synchronize(premierLeague.getId(), DATE);

        Match match = matchRepository.findByExternalId(12345L).orElseThrow();
        assertThat(summary).isEqualTo(new FixtureSyncSummary(1, 1, 0));
        assertThat(match.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(match.getHomeTeam().getName()).isEqualTo("Arsenal");
        assertThat(match.getAwayTeam().getName()).isEqualTo("Chelsea");
        assertThat(match.getLeague().getId()).isEqualTo(premierLeague.getId());
    }

    @Test
    void secondSyncUpdatesMatchWithoutDuplicatingResources() {
        when(soccerDataProvider.getFixtures("Premier League", DATE, DATE))
                .thenReturn(List.of(fixture(MatchStatus.SCHEDULED, null, null)))
                .thenReturn(List.of(fixture(MatchStatus.LIVE, 1, 0)));

        fixtureSyncService.synchronize(premierLeague.getId(), DATE);
        FixtureSyncSummary secondSummary = fixtureSyncService.synchronize(
                premierLeague.getId(), DATE
        );

        Match match = matchRepository.findByExternalId(12345L).orElseThrow();
        assertThat(secondSummary).isEqualTo(new FixtureSyncSummary(1, 0, 1));
        assertThat(matchRepository.count()).isEqualTo(1);
        assertThat(teamRepository.count()).isEqualTo(2);
        assertThat(leagueRepository.count()).isEqualTo(1);
        assertThat(match.getHomeScore()).isEqualTo(1);
        assertThat(match.getAwayScore()).isZero();
        assertThat(match.getStatus()).isEqualTo(MatchStatus.LIVE);
    }

    private ProviderFixture fixture(
            MatchStatus status,
            Integer homeScore,
            Integer awayScore
    ) {
        return new ProviderFixture(
                12345L,
                OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                status,
                new ProviderFixture.ProviderTeam(42L, "Arsenal"),
                new ProviderFixture.ProviderTeam(49L, "Chelsea"),
                homeScore,
                awayScore
        );
    }
}
