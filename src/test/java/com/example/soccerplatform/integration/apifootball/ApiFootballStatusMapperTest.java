package com.example.soccerplatform.integration.apifootball;

import com.example.soccerplatform.entity.MatchStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiFootballStatusMapperTest {

    private final ApiFootballStatusMapper mapper = new ApiFootballStatusMapper();

    @Test
    void mapsScheduledStatuses() {
        assertThat(mapper.toMatchStatus("NS")).isEqualTo(MatchStatus.SCHEDULED);
        assertThat(mapper.toMatchStatus("TBD")).isEqualTo(MatchStatus.SCHEDULED);
    }

    @Test
    void mapsLiveStatuses() {
        assertThat(mapper.toMatchStatus("1H")).isEqualTo(MatchStatus.LIVE);
        assertThat(mapper.toMatchStatus("HT")).isEqualTo(MatchStatus.LIVE);
        assertThat(mapper.toMatchStatus("2H")).isEqualTo(MatchStatus.LIVE);
    }

    @Test
    void mapsFinishedStatuses() {
        assertThat(mapper.toMatchStatus("FT")).isEqualTo(MatchStatus.FINISHED);
        assertThat(mapper.toMatchStatus("AET")).isEqualTo(MatchStatus.FINISHED);
        assertThat(mapper.toMatchStatus("PEN")).isEqualTo(MatchStatus.FINISHED);
    }

    @Test
    void mapsPostponedStatuses() {
        assertThat(mapper.toMatchStatus("PST")).isEqualTo(MatchStatus.POSTPONED);
        assertThat(mapper.toMatchStatus("CANC")).isEqualTo(MatchStatus.POSTPONED);
    }

    @Test
    void mapsUnknownAndNullStatusesToUnknown() {
        assertThat(mapper.toMatchStatus("NOT_KNOWN")).isEqualTo(MatchStatus.UNKNOWN);
        assertThat(mapper.toMatchStatus(null)).isEqualTo(MatchStatus.UNKNOWN);
    }
}
