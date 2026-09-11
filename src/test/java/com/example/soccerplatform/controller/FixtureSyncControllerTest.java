package com.example.soccerplatform.controller;

import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.dto.FixtureSyncSummary;
import com.example.soccerplatform.integration.SoccerDataProvider;
import com.example.soccerplatform.service.FixtureSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.internal-sync-api-key=test-internal-key")
@AutoConfigureMockMvc
class FixtureSyncControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FixtureSyncService fixtureSyncService;

    @MockitoBean
    private SoccerDataProvider soccerDataProvider;

    @Test
    void missingKeyIsRejected() throws Exception {
        mockMvc.perform(syncRequest())
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        verifyNoInteractions(fixtureSyncService);
    }

    @Test
    void incorrectKeyIsRejected() throws Exception {
        mockMvc.perform(syncRequest().header("X-Internal-Api-Key", "wrong-key"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        verifyNoInteractions(fixtureSyncService);
    }

    @Test
    void blankKeyIsRejected() throws Exception {
        mockMvc.perform(syncRequest().header("X-Internal-Api-Key", " "))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Unauthorized"));

        verifyNoInteractions(fixtureSyncService);
    }

    @Test
    void correctKeyReachesSyncService() throws Exception {
        LocalDate date = LocalDate.of(2026, 9, 6);
        when(fixtureSyncService.synchronize(1L, date))
                .thenReturn(new FixtureSyncSummary(1, 2, 3));

        mockMvc.perform(syncRequest().header("X-Internal-Api-Key", "test-internal-key"))
                .andExpect(status().isOk());

        verify(fixtureSyncService).synchronize(1L, date);
    }

    @Test
    void publicEndpointRemainsAccessibleWithoutInternalKey() throws Exception {
        mockMvc.perform(get("/leagues"))
                .andExpect(status().isOk());
    }

    @Test
    void providerFailureReturnsControlledError() throws Exception {
        when(fixtureSyncService.synchronize(1L, LocalDate.of(2026, 9, 6)))
                .thenThrow(new ProviderIntegrationException("Provider unavailable"));

        mockMvc.perform(syncRequest().header("X-Internal-Api-Key", "test-internal-key"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.status").value(502))
                .andExpect(jsonPath("$.message").value("Soccer data provider request failed"));
    }

    private static org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder syncRequest() {
        return post("/internal/sync/fixtures")
                .queryParam("leagueId", "1")
                .queryParam("date", "2026-09-06");
    }
}
