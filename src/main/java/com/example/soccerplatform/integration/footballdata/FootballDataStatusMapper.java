package com.example.soccerplatform.integration.footballdata;

import com.example.soccerplatform.entity.MatchStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class FootballDataStatusMapper {

    private static final Set<String> SCHEDULED = Set.of("SCHEDULED", "TIMED");
    private static final Set<String> LIVE = Set.of("IN_PLAY", "PAUSED");
    private static final Set<String> POSTPONED = Set.of(
            "POSTPONED", "SUSPENDED", "CANCELLED"
    );

    public MatchStatus toMatchStatus(String providerStatus) {
        if (providerStatus == null) {
            return MatchStatus.UNKNOWN;
        }
        if (SCHEDULED.contains(providerStatus)) {
            return MatchStatus.SCHEDULED;
        }
        if (LIVE.contains(providerStatus)) {
            return MatchStatus.LIVE;
        }
        if ("FINISHED".equals(providerStatus)) {
            return MatchStatus.FINISHED;
        }
        if (POSTPONED.contains(providerStatus)) {
            return MatchStatus.POSTPONED;
        }
        return MatchStatus.UNKNOWN;
    }
}
