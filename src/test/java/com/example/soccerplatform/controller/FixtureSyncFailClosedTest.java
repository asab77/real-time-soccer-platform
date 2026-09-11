package com.example.soccerplatform.controller;

import com.example.soccerplatform.integration.SoccerDataProvider;
import com.example.soccerplatform.service.FixtureSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.internal-sync-api-key=")
@AutoConfigureMockMvc
class FixtureSyncFailClosedTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FixtureSyncService fixtureSyncService;

    @MockitoBean
    private SoccerDataProvider soccerDataProvider;

    @Test
    void blankConfiguredKeyFailsClosedEvenWhenHeaderIsPresent() throws Exception {
        mockMvc.perform(post("/internal/sync/fixtures")
                        .header("X-Internal-Api-Key", "any-key")
                        .queryParam("leagueId", "1")
                        .queryParam("date", "2026-09-06"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        verifyNoInteractions(fixtureSyncService);
    }
}
