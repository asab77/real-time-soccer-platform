package com.example.soccerplatform.controller;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.repository.LeagueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LeagueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LeagueRepository leagueRepository;

    @Test
    void getLeaguesReturnsAvailableLeagues() throws Exception {
        League premierLeague = leagueRepository.save(new League("Premier League"));
        League laLiga = leagueRepository.save(new League("La Liga"));

        mockMvc.perform(get("/leagues"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(laLiga.getId()))
                .andExpect(jsonPath("$[0].name").value("La Liga"))
                .andExpect(jsonPath("$[1].id").value(premierLeague.getId()))
                .andExpect(jsonPath("$[1].name").value("Premier League"));
    }
}
