package com.example.soccerplatform.controller;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.LeaguePreference;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.repository.LeaguePreferenceRepository;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LeaguePreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LeagueRepository leagueRepository;

    @Autowired
    private LeaguePreferenceRepository leaguePreferenceRepository;

    @Test
    void createsLeaguePreference() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));

        mockMvc.perform(post("/users/{userId}/preferences", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leagueId": %d}
                                """.formatted(league.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.leagueId").value(league.getId()));

        assertThat(leaguePreferenceRepository.existsByUserIdAndLeagueId(
                user.getId(), league.getId()
        )).isTrue();
    }

    @Test
    void returnsNotFoundWhenUserDoesNotExist() throws Exception {
        League league = leagueRepository.save(new League("Premier League"));

        mockMvc.perform(post("/users/{userId}/preferences", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leagueId": %d}
                                """.formatted(league.getId())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void returnsNotFoundWhenLeagueDoesNotExist() throws Exception {
        User user = userRepository.save(new User("Alex"));

        mockMvc.perform(post("/users/{userId}/preferences", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leagueId": 999999}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("League not found"));
    }

    @Test
    void returnsConflictForDuplicatePreference() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));
        leaguePreferenceRepository.save(new LeaguePreference(user, league));

        mockMvc.perform(post("/users/{userId}/preferences", user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leagueId": %d}
                                """.formatted(league.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User already follows this league"));

        assertThat(leaguePreferenceRepository.count()).isEqualTo(1);
    }

    @Test
    void returnsBadRequestWhenLeagueIdIsMissing() throws Exception {
        mockMvc.perform(post("/users/{userId}/preferences", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("leagueId is required"));
    }

    @Test
    void returnsBadRequestWhenLeagueIdIsNotPositive() throws Exception {
        mockMvc.perform(post("/users/{userId}/preferences", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"leagueId": 0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("leagueId must be positive"));
    }

    @Test
    void getsFollowedLeaguesInNameOrder() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League premierLeague = leagueRepository.save(new League("Premier League"));
        League laLiga = leagueRepository.save(new League("La Liga"));
        leaguePreferenceRepository.save(new LeaguePreference(user, premierLeague));
        leaguePreferenceRepository.save(new LeaguePreference(user, laLiga));

        mockMvc.perform(get("/users/{userId}/preferences", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(laLiga.getId()))
                .andExpect(jsonPath("$[0].name").value("La Liga"))
                .andExpect(jsonPath("$[1].id").value(premierLeague.getId()))
                .andExpect(jsonPath("$[1].name").value("Premier League"));
    }

    @Test
    void getsEmptyPreferencesForUserWhoFollowsNoLeagues() throws Exception {
        User user = userRepository.save(new User("Alex"));

        mockMvc.perform(get("/users/{userId}/preferences", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getPreferencesReturnsNotFoundForMissingUser() throws Exception {
        mockMvc.perform(get("/users/{userId}/preferences", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void deletesLeaguePreference() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));
        leaguePreferenceRepository.save(new LeaguePreference(user, league));

        mockMvc.perform(delete(
                        "/users/{userId}/preferences/{leagueId}",
                        user.getId(),
                        league.getId()
                ))
                .andExpect(status().isNoContent());

        assertThat(leaguePreferenceRepository.existsByUserIdAndLeagueId(
                user.getId(), league.getId()
        )).isFalse();
    }

    @Test
    void deleteReturnsNotFoundWhenPreferenceDoesNotExist() throws Exception {
        User user = userRepository.save(new User("Alex"));
        League league = leagueRepository.save(new League("Premier League"));

        mockMvc.perform(delete(
                        "/users/{userId}/preferences/{leagueId}",
                        user.getId(),
                        league.getId()
                ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("League preference not found"));
    }
}
