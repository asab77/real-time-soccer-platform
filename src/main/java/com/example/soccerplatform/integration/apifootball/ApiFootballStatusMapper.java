package com.example.soccerplatform.integration.apifootball;

import com.example.soccerplatform.entity.MatchStatus;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class ApiFootballStatusMapper {

    private static final Set<String> LIVE_STATUSES = Set.of(
            "1H", "HT", "2H", "ET", "BT", "P", "INT", "LIVE"
    );
    private static final Set<String> FINISHED_STATUSES = Set.of(
            "FT", "AET", "PEN", "AWD", "WO"
    );
    private static final Set<String> POSTPONED_STATUSES = Set.of(
            "PST", "CANC", "ABD", "SUSP"
    );

    public MatchStatus toMatchStatus(String providerStatus) {
        if (providerStatus == null) {
            return MatchStatus.UNKNOWN;
        }
        if ("NS".equals(providerStatus) || "TBD".equals(providerStatus)) {
            return MatchStatus.SCHEDULED;
        }
        if (LIVE_STATUSES.contains(providerStatus)) {
            return MatchStatus.LIVE;
        }
        if (FINISHED_STATUSES.contains(providerStatus)) {
            return MatchStatus.FINISHED;
        }
        if (POSTPONED_STATUSES.contains(providerStatus)) {
            return MatchStatus.POSTPONED;
        }

        return MatchStatus.UNKNOWN;
    }
}
