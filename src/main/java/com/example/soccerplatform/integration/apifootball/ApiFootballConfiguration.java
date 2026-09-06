package com.example.soccerplatform.integration.apifootball;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ApiFootballProperties.class)
public class ApiFootballConfiguration {

    @Bean
    RestClient apiFootballRestClient(
            RestClient.Builder builder,
            ApiFootballProperties properties
    ) {
        return builder.baseUrl(properties.baseUrl()).build();
    }
}
