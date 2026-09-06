package com.example.soccerplatform.integration.apifootball;

import com.example.soccerplatform.exception.ProviderIntegrationException;
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

class ApiFootballClientTest {

    private static final String TEST_KEY = "test-key";

    private MockRestServiceServer server;
    private ApiFootballClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();

        ApiFootballProperties properties = new ApiFootballProperties(
                "https://v3.football.api-sports.io",
                TEST_KEY,
                List.of()
        );
        client = new ApiFootballClient(builder.baseUrl(properties.baseUrl()).build(), properties);
    }

    @Test
    void requestsFixturesAndDeserializesDocumentedResponseShape() {
        server.expect(once(), requestTo(
                        "https://v3.football.api-sports.io/fixtures?league=39&season=2026&date=2026-09-06"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("x-apisports-key", TEST_KEY))
                .andRespond(withSuccess("""
                        {
                          "errors": [],
                          "response": [{
                            "fixture": {
                              "id": 12345,
                              "date": "2026-09-06T15:00:00+00:00",
                              "status": {"short": "NS"}
                            },
                            "league": {"id": 39, "name": "Premier League"},
                            "teams": {
                              "home": {"id": 42, "name": "Arsenal"},
                              "away": {"id": 49, "name": "Chelsea"}
                            },
                            "goals": {"home": null, "away": null}
                          }]
                        }
                        """, MediaType.APPLICATION_JSON));

        List<ApiFootballFixture> fixtures = client.getFixtures(39L, 2026, LocalDate.of(2026, 9, 6));

        assertThat(fixtures).hasSize(1);
        ApiFootballFixture result = fixtures.getFirst();
        assertThat(result.fixture().id()).isEqualTo(12345L);
        assertThat(result.fixture().date()).isEqualTo(OffsetDateTime.parse("2026-09-06T15:00:00Z"));
        assertThat(result.fixture().status().shortCode()).isEqualTo("NS");
        assertThat(result.league().id()).isEqualTo(39L);
        assertThat(result.league().name()).isEqualTo("Premier League");
        assertThat(result.teams().home().id()).isEqualTo(42L);
        assertThat(result.teams().home().name()).isEqualTo("Arsenal");
        assertThat(result.teams().away().id()).isEqualTo(49L);
        assertThat(result.teams().away().name()).isEqualTo("Chelsea");
        assertThat(result.goals().home()).isNull();
        assertThat(result.goals().away()).isNull();
        server.verify();
    }

    @Test
    void preservesProviderHttpStatusAsTheExceptionCause() {
        server.expect(once(), requestTo(
                        "https://v3.football.api-sports.io/fixtures?league=39&season=2026&date=2026-09-06"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        assertThatThrownBy(() -> client.getFixtures(39L, 2026, LocalDate.of(2026, 9, 6)))
                .isInstanceOf(ProviderIntegrationException.class)
                .hasCauseInstanceOf(RestClientResponseException.class)
                .satisfies(exception -> {
                    RestClientResponseException cause =
                            (RestClientResponseException) exception.getCause();
                    assertThat(cause.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                });
        server.verify();
    }
}
