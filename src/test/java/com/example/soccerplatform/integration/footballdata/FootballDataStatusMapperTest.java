package com.example.soccerplatform.integration.footballdata;

import com.example.soccerplatform.entity.MatchStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FootballDataStatusMapperTest {

    private final FootballDataStatusMapper mapper = new FootballDataStatusMapper();

    @Test
    void mapsDocumentedStatuses() {
        assertThat(mapper.toMatchStatus("SCHEDULED")).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(mapper.toMatchStatus("TIMED")).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(mapper.toMatchStatus("IN_PLAY")).isEqualTo(MatchStatus.LIVE);
        assertThat(mapper.toMatchStatus("PAUSED")).isEqualTo(MatchStatus.LIVE);
        assertThat(mapper.toMatchStatus("FINISHED")).isEqualTo(MatchStatus.FINISHED);
        assertThat(mapper.toMatchStatus("POSTPONED")).isEqualTo(MatchStatus.POSTPONED);
        assertThat(mapper.toMatchStatus("SUSPENDED")).isEqualTo(MatchStatus.POSTPONED);
        assertThat(mapper.toMatchStatus("CANCELLED")).isEqualTo(MatchStatus.POSTPONED);
        assertThat(mapper.toMatchStatus("UNKNOWN_PROVIDER_VALUE"))
                .isEqualTo(MatchStatus.UNKNOWN);
        assertThat(mapper.toMatchStatus(null)).isEqualTo(MatchStatus.UNKNOWN);
    }
}
