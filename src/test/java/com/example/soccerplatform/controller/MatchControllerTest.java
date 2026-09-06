package com.example.soccerplatform.controller;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.LeaguePreference;
import com.example.soccerplatform.entity.Match;
import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.entity.Team;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.MatchRepository;
import com.example.soccerplatform.repository.TeamRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private LeaguePreferenceRepository leaguePreferenceRepository;

    @Test
    void getsMatchesForRequestedLeagueInStartTimeOrder() throws Exception {
        League requestedLeague = leagueRepository.save(new League("Premier League"));
        League otherLeague = leagueRepository.save(new League("La Liga"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Chelsea"));

        Match laterMatch = saveMatch(requestedLeague, homeTeam, awayTeam,
                "2026-09-12T18:00:00Z", MatchStatus.SCHEDULED);
        Match earlierMatch = saveMatch(requestedLeague, awayTeam, homeTeam,
                "2026-09-12T15:00:00Z", MatchStatus.LIVE);
        saveMatch(otherLeague, homeTeam, awayTeam,
                "2026-09-12T14:00:00Z", MatchStatus.LIVE);

        mockMvc.perform(get("/leagues/{leagueId}/matches", requestedLeague.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(earlierMatch.getId()))
                .andExpect(jsonPath("$[0].leagueName").value("Premier League"))
                .andExpect(jsonPath("$[0].homeTeamName").value("Chelsea"))
                .andExpect(jsonPath("$[1].id").value(laterMatch.getId()));
    }

    @Test
    void filtersLeagueMatchesByStatus() throws Exception {
        League league = leagueRepository.save(new League("Premier League"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Chelsea"));
        Match liveMatch = saveMatch(league, homeTeam, awayTeam,
                "2026-09-12T15:00:00Z", MatchStatus.LIVE);
        saveMatch(league, awayTeam, homeTeam,
                "2026-09-13T15:00:00Z", MatchStatus.SCHEDULED);

        mockMvc.perform(get("/leagues/{leagueId}/matches", league.getId())
                        .queryParam("status", "LIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(liveMatch.getId()))
                .andExpect(jsonPath("$[0].status").value("LIVE"));
    }

    @Test
    void invalidMatchStatusReturnsBadRequest() throws Exception {
        League league = leagueRepository.save(new League("Premier League"));

        mockMvc.perform(get("/leagues/{leagueId}/matches", league.getId())
                        .queryParam("status", "NOT_REAL"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid status"));
    }

    @Test
    void missingLeagueReturnsNotFound() throws Exception {
        mockMvc.perform(get("/leagues/{leagueId}/matches", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("League not found"));
    }

    @Test
    void getsOnlyMatchesFromUsersFollowedLeagues() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League followedLeague = leagueRepository.save(new League("Premier League"));
        League unfollowedLeague = leagueRepository.save(new League("La Liga"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Chelsea"));
        leaguePreferenceRepository.save(new LeaguePreference(user, followedLeague));

        Match followedMatch = saveMatch(followedLeague, homeTeam, awayTeam,
                "2026-09-12T15:00:00Z", MatchStatus.LIVE);
        saveMatch(unfollowedLeague, awayTeam, homeTeam,
                "2026-09-12T14:00:00Z", MatchStatus.LIVE);

        mockMvc.perform(get("/users/{userId}/matches", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(followedMatch.getId()))
                .andExpect(jsonPath("$[0].leagueId").value(followedLeague.getId()));
    }

    @Test
    void filtersUserMatchesByStatus() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));
        Team homeTeam = teamRepository.save(new Team("Arsenal"));
        Team awayTeam = teamRepository.save(new Team("Chelsea"));
        leaguePreferenceRepository.save(new LeaguePreference(user, league));

        Match liveMatch = saveMatch(league, homeTeam, awayTeam,
                "2026-09-12T15:00:00Z", MatchStatus.LIVE);
        saveMatch(league, awayTeam, homeTeam,
                "2026-09-13T15:00:00Z", MatchStatus.FINISHED);

        mockMvc.perform(get("/users/{userId}/matches", user.getId())
                        .queryParam("status", "LIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(liveMatch.getId()))
                .andExpect(jsonPath("$[0].status").value("LIVE"));
    }

    @Test
    void getsEmptyMatchesWhenUserFollowsNoLeagues() throws Exception {
        User user = userRepository.save(new User("Alex"));

        mockMvc.perform(get("/users/{userId}/matches", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void missingUserReturnsNotFound() throws Exception {
        mockMvc.perform(get("/users/{userId}/matches", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    private Match saveMatch(
            League league,
            Team homeTeam,
            Team awayTeam,
            String startTime,
            MatchStatus status
    ) {
        Match match = new Match(
                league,
                homeTeam,
                awayTeam,
                OffsetDateTime.parse(startTime),
                status
        );
        return matchRepository.save(match);
    }
}
