package com.example.soccerplatform.config;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.entity.User;
import com.example.soccerplatform.integration.apifootball.ApiFootballProperties;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.repository.UserRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.demo-data.enabled", havingValue = "true", matchIfMissing = true)
public class DemoDataInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final LeagueRepository leagues;
    private final ApiFootballProperties properties;

    public DemoDataInitializer(
            UserRepository users,
            LeagueRepository leagues,
            ApiFootballProperties properties) {
        this.users = users;
        this.leagues = leagues;
        this.properties = properties;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean demoUserExists = users.findAll().stream()
                .anyMatch(user -> user.getName().equals("Demo User"));

        if (!demoUserExists) {
            users.save(new User("Demo User"));
        }

        for (var configuredLeague : properties.leagues()) {
            leagues.findByExternalId(configuredLeague.externalId()).orElseGet(() -> {
                League league = new League(configuredLeague.name());
                league.setExternalId(configuredLeague.externalId());
                return leagues.save(league);
            });
        }
    }
}
