package com.example.soccerplatform.event;

import com.example.soccerplatform.cache.MatchCacheInvalidationEvent;
import com.example.soccerplatform.cache.MatchCacheInvalidationListener;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static org.assertj.core.api.Assertions.assertThat;

class AfterCommitEventListenerTest {

    @Test
    void matchUpdatesArePublishedAfterCommit() throws NoSuchMethodException {
        TransactionalEventListener annotation = MatchUpdateWebSocketListener.class
                .getMethod("broadcast", MatchUpdatedEvent.class)
                .getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }

    @Test
    void matchCachesAreInvalidatedAfterCommit() throws NoSuchMethodException {
        TransactionalEventListener annotation = MatchCacheInvalidationListener.class
                .getMethod("invalidate", MatchCacheInvalidationEvent.class)
                .getAnnotation(TransactionalEventListener.class);

        assertThat(annotation).isNotNull();
        assertThat(annotation.phase()).isEqualTo(TransactionPhase.AFTER_COMMIT);
    }
}
