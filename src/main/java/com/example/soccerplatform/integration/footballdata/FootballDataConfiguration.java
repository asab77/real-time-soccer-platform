package com.example.soccerplatform.integration.footballdata;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(FootballDataProperties.class)
@ConditionalOnProperty(
        name = "soccer.data.provider",
        havingValue = "football-data",
        matchIfMissing = true
)
public class FootballDataConfiguration {

    @Bean
    RestClient footballDataRestClient(
            RestClient.Builder builder,
            FootballDataProperties properties
    ) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
