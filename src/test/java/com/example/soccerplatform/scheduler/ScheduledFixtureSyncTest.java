package com.example.soccerplatform.scheduler;

import com.example.soccerplatform.integration.apifootball.ApiFootballProperties;
import com.example.soccerplatform.service.FixtureSyncService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        ApiFootballProperties properties = new ApiFootballProperties("base", "", List.of(
                new ApiFootballProperties.ConfiguredLeague(39L, "Premier League"),
                new ApiFootballProperties.ConfiguredLeague(140L, "La Liga")
        ));
        ScheduledFixtureSync scheduler = new ScheduledFixtureSync(
                service, properties, LocalTime.MIN, LocalTime.MAX
        );

        scheduler.synchronizeToday();

        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        verify(service).synchronize(39L, today.getYear(), today);
        verify(service).synchronize(140L, today.getYear(), today);
    }
}
