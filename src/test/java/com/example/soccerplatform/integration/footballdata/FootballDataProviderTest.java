package com.example.soccerplatform.integration.footballdata;

import com.example.soccerplatform.entity.MatchStatus;
import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.integration.ProviderFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class FootballDataProviderTest {

    private static final String TEST_KEY = "test-football-data-key";
    private static final String BASE_URL = "https://api.football-data.org/v4";
    private static final LocalDate DATE = LocalDate.of(2026, 9, 6);

    private MockRestServiceServer server;
    private FootballDataProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        FootballDataProperties properties = properties(TEST_KEY);
        provider = new FootballDataProvider(
                builder.baseUrl(BASE_URL).build(),
                properties,
                new FootballDataStatusMapper()
        );
    }

    @Test
    void sendsTokenAndDateRangeThenMapsDocumentedResponse() {
        server.expect(once(), requestTo(
                        BASE_URL + "/competitions/PL/matches?dateFrom=2026-09-06&dateTo=2026-09-06"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Auth-Token", TEST_KEY))
                .andRespond(withSuccess("""
                        {
                          "matches": [{
                            "id": 54321,
                            "utcDate": "2026-09-06T15:00:00Z",
                            "status": "IN_PLAY",
                            "homeTeam": {"id": 57, "name": "Arsenal FC"},
                            "awayTeam": {"id": 61, "name": "Chelsea FC"},
                            "score": {"fullTime": {"home": 1, "away": 0}}
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<ProviderFixture> fixtures = provider.getFixtures("Premier League", DATE, DATE);

        assertThat(fixtures).containsExactly(new ProviderFixture(
                54321L,
                OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                MatchStatus.LIVE,
                new ProviderFixture.ProviderTeam(57L, "Arsenal FC"),
                new ProviderFixture.ProviderTeam(61L, "Chelsea FC"),
                1,
                0
        ));
        server.verify();
    }

    @Test
    void blankApiKeyFailsBeforeSendingARequest() {
        FootballDataProperties properties = properties(" ");
        FootballDataProvider providerWithoutKey = new FootballDataProvider(
                RestClient.builder().baseUrl(BASE_URL).build(),
                properties,
                new FootballDataStatusMapper()
        );

        assertThatThrownBy(() -> providerWithoutKey.getFixtures("Premier League", DATE, DATE))
                .isInstanceOf(ProviderIntegrationException.class)
                .hasMessageContaining("key is not configured");
    }

    @Test
    void providerHttpErrorBecomesControlledIntegrationException() {
        server.expect(once(), requestTo(
                        BASE_URL + "/competitions/PL/matches?dateFrom=2026-09-06&dateTo=2026-09-06"))
                .andRespond(withStatus(HttpStatus.FORBIDDEN));

        assertThatThrownBy(() -> provider.getFixtures("Premier League", DATE, DATE))
                .isInstanceOf(ProviderIntegrationException.class)
                .hasCauseInstanceOf(RestClientResponseException.class);
        server.verify();
    }

    private FootballDataProperties properties(String key) {
        return new FootballDataProperties(BASE_URL, key, List.of(
                new FootballDataProperties.Competition("PL", "Premier League"),
                new FootballDataProperties.Competition("PD", "La Liga"),
                new FootballDataProperties.Competition("BL1", "Bundesliga"),
                new FootballDataProperties.Competition("SA", "Serie A"),
                new FootballDataProperties.Competition("FL1", "Ligue 1")
        ));
    }
}
