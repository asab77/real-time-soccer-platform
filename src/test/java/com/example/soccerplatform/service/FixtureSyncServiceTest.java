package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.integration.apifootball.ApiFootballClient;
import com.example.soccerplatform.integration.apifootball.ApiFootballFixture;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
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

    private static final long PREMIER_LEAGUE_ID = 39L;
    private static final int SEASON = 2026;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 6);

    @Autowired
    private FixtureSyncService fixtureSyncService;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @MockitoBean
    private ApiFootballClient apiFootballClient;

    @Test
    void createsMatchFromProviderFixture() {
        when(apiFootballClient.getFixtures(PREMIER_LEAGUE_ID, SEASON, DATE))
                .thenReturn(List.of(fixture("NS", null, null)));

        FixtureSyncSummary summary = fixtureSyncService.synchronize(
                PREMIER_LEAGUE_ID,
                SEASON,
                DATE
        );

        Match match = matchRepository.findByExternalId(12345L).orElseThrow();
        assertThat(summary).isEqualTo(new FixtureSyncSummary(1, 1, 0));
        assertThat(match.getStatus()).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(match.getHomeTeam().getName()).isEqualTo("Arsenal");
        assertThat(match.getAwayTeam().getName()).isEqualTo("Chelsea");
    }

    @Test
    void secondSyncUpdatesMatchWithoutDuplicatingResources() {
        ApiFootballFixture scheduled = fixture("NS", null, null);
        ApiFootballFixture live = fixture("1H", 1, 0);
        when(apiFootballClient.getFixtures(PREMIER_LEAGUE_ID, SEASON, DATE))
                .thenReturn(List.of(scheduled))
                .thenReturn(List.of(live));

        fixtureSyncService.synchronize(PREMIER_LEAGUE_ID, SEASON, DATE);
        FixtureSyncSummary secondSummary = fixtureSyncService.synchronize(
                PREMIER_LEAGUE_ID,
                SEASON,
                DATE
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

    private ApiFootballFixture fixture(String status, Integer homeScore, Integer awayScore) {
        return new ApiFootballFixture(
                new ApiFootballFixture.Fixture(
                        12345L,
                        OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                        new ApiFootballFixture.Status(status)
                ),
                new ApiFootballFixture.League(PREMIER_LEAGUE_ID, "Premier League"),
                new ApiFootballFixture.Teams(
                        new ApiFootballFixture.Team(42L, "Arsenal"),
                        new ApiFootballFixture.Team(49L, "Chelsea")
                ),
                new ApiFootballFixture.Goals(homeScore, awayScore)
        );
    }
}
