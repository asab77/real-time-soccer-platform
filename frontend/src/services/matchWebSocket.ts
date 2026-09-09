import { Client, type StompSubscription } from "@stomp/stompjs";
import { apiBaseUrl } from "../api/client";
import type { Match } from "../types";
export function connectToMatchUpdates(
  leagueIds: number[],
  onUpdate: (match: Match) => void,
  onConnection: (connected: boolean) => void,
) {
  const subscriptions: StompSubscription[] = [];
  const client = new Client({
    brokerURL: apiBaseUrl.replace(/^http/, "ws") + "/ws",
    reconnectDelay: 5000,
    onConnect: () => {
      onConnection(true);
      leagueIds.forEach((id) =>
        subscriptions.push(
          client.subscribe(`/topic/leagues/${id}/matches`, (message) =>
            onUpdate(JSON.parse(message.body)),
          ),
        ),
      );
    },
    onWebSocketClose: () => onConnection(false),
    onStompError: () => onConnection(false),
  });
  client.activate();
  return () => {
    subscriptions.forEach((s) => s.unsubscribe());
    void client.deactivate();
  };
}
