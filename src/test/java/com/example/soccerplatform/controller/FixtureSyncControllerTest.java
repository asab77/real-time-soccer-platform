package com.example.soccerplatform.controller;

import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.integration.apifootball.ApiFootballClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FixtureSyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiFootballClient apiFootballClient;

    @Test
    void providerFailureReturnsControlledError() throws Exception {
        when(apiFootballClient.getFixtures(
                39L,
                2026,
                LocalDate.of(2026, 9, 6)
        )).thenThrow(new ProviderIntegrationException("Provider unavailable"));

        mockMvc.perform(post("/internal/sync/fixtures")
                        .queryParam("externalLeagueId", "39")
                        .queryParam("season", "2026")
                        .queryParam("date", "2026-09-06"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Soccer data provider request failed"));
    }
}
