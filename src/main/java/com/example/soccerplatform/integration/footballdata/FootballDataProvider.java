package com.example.soccerplatform.integration.footballdata;

import com.example.soccerplatform.exception.InvalidSyncRequestException;
import com.example.soccerplatform.exception.ProviderIntegrationException;
import com.example.soccerplatform.integration.ProviderFixture;
import com.example.soccerplatform.integration.SoccerDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Component
@ConditionalOnProperty(
        name = "soccer.data.provider",
        havingValue = "football-data",
        matchIfMissing = true
)
public class FootballDataProvider implements SoccerDataProvider {

    private static final String API_KEY_HEADER = "X-Auth-Token";
    private static final Logger logger = LoggerFactory.getLogger(FootballDataProvider.class);

    private final RestClient restClient;
    private final FootballDataProperties properties;
    private final FootballDataStatusMapper statusMapper;

    public FootballDataProvider(
            RestClient footballDataRestClient,
            FootballDataProperties properties,
            FootballDataStatusMapper statusMapper
    ) {
        this.restClient = footballDataRestClient;
        this.properties = properties;
        this.statusMapper = statusMapper;
    }

    @Override
    public List<ProviderFixture> getFixtures(
            String leagueName,
            LocalDate dateFrom,
            LocalDate dateTo
    ) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            logger.warn("football-data.org request was not sent because the API key is not configured");
            throw new ProviderIntegrationException("football-data.org key is not configured");
        }

        String competitionCode = competitionCodeFor(leagueName);
        try {
            FootballDataResponse body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/competitions/{competitionCode}/matches")
                            .queryParam("dateFrom", dateFrom)
                            .queryParam("dateTo", dateTo)
                            .build(competitionCode))
                    .header(API_KEY_HEADER, properties.apiKey())
                    .retrieve()
                    .body(FootballDataResponse.class);

            if (body == null || body.matches() == null) {
                logger.warn("football-data.org matches endpoint returned an invalid response");
                throw new ProviderIntegrationException("football-data.org returned an invalid response");
            }
            return body.matches().stream().map(this::toProviderFixture).toList();
        } catch (ProviderIntegrationException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            logger.warn(
                    "football-data.org matches request failed with HTTP status {} ({})",
                    exception.getStatusCode().value(),
                    exception.getClass().getSimpleName()
            );
            throw new ProviderIntegrationException("football-data.org request failed", exception);
        } catch (RestClientException exception) {
            logger.warn(
                    "football-data.org matches request failed ({})",
                    exception.getClass().getSimpleName()
            );
            throw new ProviderIntegrationException("football-data.org request failed", exception);
        }
    }

    private String competitionCodeFor(String leagueName) {
        return configuredCompetitions().stream()
                .filter(competition -> competition.name().equalsIgnoreCase(leagueName))
                .map(FootballDataProperties.Competition::code)
                .findFirst()
                .orElseThrow(() -> new InvalidSyncRequestException(
                        "League is not configured for synchronization"
                ));
    }

    private List<FootballDataProperties.Competition> configuredCompetitions() {
        return properties.competitions() == null ? List.of() : properties.competitions();
    }

    private ProviderFixture toProviderFixture(FootballDataMatch match) {
        validateMatch(match);
        FootballDataMatch.FullTime fullTime = match.score() == null
                ? null
                : match.score().fullTime();
        return new ProviderFixture(
                match.id(),
                match.utcDate(),
                statusMapper.toMatchStatus(match.status()),
                new ProviderFixture.ProviderTeam(match.homeTeam().id(), match.homeTeam().name()),
                new ProviderFixture.ProviderTeam(match.awayTeam().id(), match.awayTeam().name()),
                fullTime == null ? null : fullTime.home(),
                fullTime == null ? null : fullTime.away()
        );
    }

    private void validateMatch(FootballDataMatch match) {
        boolean invalid = match == null
                || match.id() == null
                || match.utcDate() == null
                || invalidTeam(match.homeTeam())
                || invalidTeam(match.awayTeam());
        if (invalid) {
            throw new ProviderIntegrationException("football-data.org returned malformed match data");
        }
    }

    private boolean invalidTeam(FootballDataMatch.Team team) {
        return team == null || team.id() == null || team.name() == null;
    }
}
