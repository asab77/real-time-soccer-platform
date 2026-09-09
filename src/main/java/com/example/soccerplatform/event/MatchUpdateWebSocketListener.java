package com.example.soccerplatform.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class MatchUpdateWebSocketListener {
    private static final Logger logger = LoggerFactory.getLogger(MatchUpdateWebSocketListener.class);
    private final SimpMessagingTemplate messagingTemplate;

    public MatchUpdateWebSocketListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void broadcast(MatchUpdatedEvent event) {
        String topic = "/topic/leagues/" + event.message().leagueId() + "/matches";
        try {
            messagingTemplate.convertAndSend(topic, event.message());
        } catch (RuntimeException exception) {
            logger.warn("WebSocket broadcast failed for league {} ({})",
                    event.message().leagueId(), exception.getClass().getSimpleName());
        }
    }
}
