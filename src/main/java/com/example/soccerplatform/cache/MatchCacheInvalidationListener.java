package com.example.soccerplatform.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MatchCacheInvalidationListener {

    private static final Logger logger = LoggerFactory.getLogger(MatchCacheInvalidationListener.class);

    private final CacheManager cacheManager;

    public MatchCacheInvalidationListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidate(MatchCacheInvalidationEvent event) {
        if (event.leagueMatches()) {
            clear("leagueMatches");
        }
        if (event.userMatches()) {
            clear("userMatches");
        }
    }

    private void clear(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache == null) {
            return;
        }

        try {
            cache.clear();
            logger.info("Invalidated {} cache after database commit", cacheName);
        } catch (RuntimeException exception) {
            logger.warn(
                    "Could not invalidate {} cache ({}); entries will expire by TTL",
                    cacheName,
                    exception.getClass().getSimpleName()
            );
        }
    }
}
