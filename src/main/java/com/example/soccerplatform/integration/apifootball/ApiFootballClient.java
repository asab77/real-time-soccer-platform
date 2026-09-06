package com.example.soccerplatform.integration.apifootball;

import com.example.soccerplatform.exception.ProviderIntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.time.LocalDate;
import java.util.List;

@Component
public class ApiFootballClient {

    private static final String API_KEY_HEADER = "x-apisports-key";
    private static final Logger logger = LoggerFactory.getLogger(ApiFootballClient.class);

    private final RestClient restClient;
    private final ApiFootballProperties properties;

    public ApiFootballClient(RestClient apiFootballRestClient, ApiFootballProperties properties) {
        this.restClient = apiFootballRestClient;
        this.properties = properties;
    }

    public List<ApiFootballFixture> getFixtures(
            Long externalLeagueId,
            int season,
            LocalDate date
    ) {
        if (properties.key() == null || properties.key().isBlank()) {
            logger.warn("API-Football request to /fixtures was not sent because the API key is not configured");
            throw new ProviderIntegrationException("API-Football key is not configured");
        }

        try {
            ApiFootballResponse body = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/fixtures")
                            .queryParam("league", externalLeagueId)
                            .queryParam("season", season)
                            .queryParam("date", date)
                            .build())
                    .header(API_KEY_HEADER, properties.key())
                    .retrieve()
                    .body(ApiFootballResponse.class);

            if (body == null || body.response() == null) {
                logger.warn("API-Football /fixtures returned a response without a response collection");
                throw new ProviderIntegrationException("API-Football returned an invalid response");
            }

            if (body.errors() != null && !body.errors().isEmpty()) {
                logger.warn("API-Football /fixtures returned application-level errors");
                throw new ProviderIntegrationException("API-Football rejected the request");
            }

            return body.response();
        } catch (ProviderIntegrationException exception) {
            throw exception;
        } catch (RestClientResponseException exception) {
            logger.warn(
                    "API-Football request to /fixtures failed with provider HTTP status {} ({})",
                    exception.getStatusCode().value(),
                    exception.getClass().getSimpleName()
            );
            throw new ProviderIntegrationException("API-Football request failed", exception);
        } catch (RestClientException exception) {
            logger.warn(
                    "API-Football request to /fixtures failed before a valid provider response "
                            + "(exception={}, rootCause={})",
                    exception.getClass().getSimpleName(),
                    rootCauseType(exception)
            );
            throw new ProviderIntegrationException("API-Football request failed", exception);
        }
    }

    private String rootCauseType(Throwable exception) {
        Throwable rootCause = exception;
        while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getClass().getSimpleName();
    }
}
