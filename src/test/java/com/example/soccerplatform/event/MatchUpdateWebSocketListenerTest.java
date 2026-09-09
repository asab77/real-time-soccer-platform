package com.example.soccerplatform.event;

import com.example.soccerplatform.dto.MatchUpdateMessage;
import com.example.soccerplatform.dto.MatchUpdateType;
import com.example.soccerplatform.entity.MatchStatus;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.OffsetDateTime;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MatchUpdateWebSocketListenerTest {
    @Test
    void broadcastsUpdatedMessageToLeagueTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        MatchUpdateWebSocketListener listener = new MatchUpdateWebSocketListener(template);
        MatchUpdateMessage message = new MatchUpdateMessage(
                10L, 1L, 2L, "Arsenal", 3L, "Chelsea",
                OffsetDateTime.parse("2026-09-06T15:00:00Z"),
                1, 0, MatchStatus.LIVE, MatchUpdateType.UPDATED
        );

        listener.broadcast(new MatchUpdatedEvent(message));

        verify(template).convertAndSend("/topic/leagues/1/matches", message);
    }
}
