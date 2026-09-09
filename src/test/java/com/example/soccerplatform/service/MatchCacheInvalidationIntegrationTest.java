package com.example.soccerplatform.service;

import com.example.soccerplatform.dto.CreateLeaguePreferenceRequest;
import com.example.soccerplatform.dto.MatchResponse;
import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.integration.apifootball.ApiFootballClient;
import com.example.soccerplatform.integration.apifootball.ApiFootballFixture;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
class MatchCacheInvalidationIntegrationTest {

    private static final long EXTERNAL_LEAGUE_ID = 39L;
    private static final int SEASON = 2026;
    private static final LocalDate DATE = LocalDate.of(2026, 9, 6);

    @Autowired
    private FixtureSyncService fixtureSyncService;

    @Autowired
    private MatchService matchService;

    @Autowired
    private LeaguePreferenceService leaguePreferenceService;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeaguePreferenceRepository leaguePreferenceRepository;

    @Autowired
    private CacheManager cacheManager;

    @MockitoBean
    private ApiFootballClient apiFootballClient;

    @BeforeEach
    void cleanState() {
        clearCache("leagueMatches");
        clearCache("userMatches");
        leaguePreferenceRepository.deleteAll();
        matchRepository.deleteAll();
        userRepository.deleteAll();
        teamRepository.deleteAll();
        leagueRepository.deleteAll();
    }

    @Test
    void fixtureSyncEvictsCachedMatchesAfterCommit() {
        when(apiFootballClient.getFixtures(EXTERNAL_LEAGUE_ID, SEASON, DATE))
                .thenReturn(List.of(providerFixture("NS", null, null)))
                .thenReturn(List.of(providerFixture("1H", 1, 0)));

        fixtureSyncService.synchronize(EXTERNAL_LEAGUE_ID, SEASON, DATE);
        League league = leagueRepository.findByExternalId(EXTERNAL_LEAGUE_ID).orElseThrow();

        List<MatchResponse> cachedScheduled = matchService.getLeagueMatches(
                league.getId(), null
        );
        assertThat(cachedScheduled.getFirst().status()).isEqualTo(MatchStatus.SCHEDULED);

        fixtureSyncService.synchronize(EXTERNAL_LEAGUE_ID, SEASON, DATE);

        List<MatchResponse> refreshed = matchService.getLeagueMatches(league.getId(), null);
        assertThat(refreshed.getFirst().status()).isEqualTo(MatchStatus.LIVE);
        assertThat(refreshed.getFirst().homeScore()).isEqualTo(1);
        assertThat(refreshed.getFirst().awayScore()).isZero();
    }

    @Test
    void preferenceChangesEvictCachedUserMatchesAfterCommit() {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Chelsea"));
        matchRepository.save(new Match(
                league,
                homeTeam,
                awayTeam,
                OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                MatchStatus.LIVE
        ));

        assertThat(matchService.getUserMatches(user.getId(), null)).isEmpty();

        leaguePreferenceService.createPreference(
                user.getId(), new CreateLeaguePreferenceRequest(league.getId())
        );
        assertThat(matchService.getUserMatches(user.getId(), null)).hasSize(1);

        leaguePreferenceService.deletePreference(user.getId(), league.getId());
        assertThat(matchService.getUserMatches(user.getId(), null)).isEmpty();
    }

    private ApiFootballFixture providerFixture(
            String status,
            Integer homeScore,
            Integer awayScore
    ) {
        return new ApiFootballFixture(
                new ApiFootballFixture.Fixture(
                        12345L,
                        OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                        new ApiFootballFixture.Status(status)
                ),
                new ApiFootballFixture.League(EXTERNAL_LEAGUE_ID, "Premier League"),
                new ApiFootballFixture.Teams(
                        new ApiFootballFixture.Team(42L, "Arsenal"),
                        new ApiFootballFixture.Team(49L, "Chelsea")
                ),
                new ApiFootballFixture.Goals(homeScore, awayScore)
        );
    }

    private void clearCache(String name) {
        if (cacheManager.getCache(name) != null) {
            cacheManager.getCache(name).clear();
        }
    }
}
