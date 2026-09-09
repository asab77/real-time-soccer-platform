export type MatchStatus =
  | "SCHEDULED"
  | "LIVE"
  | "FINISHED"
  | "POSTPONED"
  | "UNKNOWN";
export type League = { id: number; name: string };
export type DemoUser = { id: number; name: string };
export type Match = {
  matchId?: number;
  id?: number;
  leagueId: number;
  leagueName?: string;
  homeTeamId: number;
  homeTeamName: string;
  awayTeamId: number;
  awayTeamName: string;
  startTime: string;
  homeScore: number | null;
  awayScore: number | null;
  status: MatchStatus;
  updateType?: "CREATED" | "UPDATED";
};
export type Filter = "ALL" | "LIVE" | "SCHEDULED" | "FINISHED";
