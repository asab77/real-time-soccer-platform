package com.example.soccerplatform.scheduler;

import com.example.soccerplatform.entity.League;
import com.example.soccerplatform.integration.footballdata.FootballDataProperties;
import com.example.soccerplatform.repository.LeagueRepository;
import com.example.soccerplatform.service.FixtureSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class ScheduledFixtureSyncTest {
    @Autowired ApplicationContext context;

    @Test
    void schedulerIsDisabledInTests() {
        assertThat(context.getBeansOfType(ScheduledFixtureSync.class)).isEmpty();
    }

    @Test
    void scheduledRunDelegatesForEveryConfiguredLeague() {
        FixtureSyncService service = mock(FixtureSyncService.class);
        LeagueRepository repository = mock(LeagueRepository.class);
        FootballDataProperties properties = new FootballDataProperties("base", "", List.of(
                new FootballDataProperties.Competition("PL", "Premier League"),
                new FootballDataProperties.Competition("PD", "La Liga")
        ));
        League premierLeague = mock(League.class);
        League laLiga = mock(League.class);
        when(premierLeague.getId()).thenReturn(1L);
        when(laLiga.getId()).thenReturn(2L);
        when(repository.findByNameIgnoreCase("Premier League"))
                .thenReturn(Optional.of(premierLeague));
        when(repository.findByNameIgnoreCase("La Liga"))
                .thenReturn(Optional.of(laLiga));

        ScheduledFixtureSync scheduler = new ScheduledFixtureSync(
                service, properties, repository, LocalTime.MIN, LocalTime.MAX
        );

        scheduler.synchronizeToday();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        verify(service).synchronize(1L, today);
        verify(service).synchronize(2L, today);
    }
}
