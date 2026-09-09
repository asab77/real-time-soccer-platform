package com.example.soccerplatform.cache;

public record MatchCacheInvalidationEvent(
        boolean leagueMatches,
        boolean userMatches
) {
    public static MatchCacheInvalidationEvent allMatchCaches() {
        return new MatchCacheInvalidationEvent(true, true);
    }

    public static MatchCacheInvalidationEvent userMatchCache() {
        return new MatchCacheInvalidationEvent(false, true);
    }
}
