import type { Match } from "../types";
const labels = {
  LIVE: "LIVE",
  FINISHED: "FT",
  SCHEDULED: "UPCOMING",
  POSTPONED: "POSTPONED",
  UNKNOWN: "STATUS PENDING",
};
export function MatchCard({ match }: { match: Match }) {
  const scores = match.homeScore != null && match.awayScore != null;
  return (
    <article className={`match-card ${match.status.toLowerCase()}`}>
      <div className="match-meta">
        <span>{match.leagueName ?? "League"}</span>
        <strong>{labels[match.status]}</strong>
      </div>
      <div className="teams">
        <span>{match.homeTeamName}</span>
        <b>{scores ? `${match.homeScore} – ${match.awayScore}` : "vs"}</b>
        <span>{match.awayTeamName}</span>
      </div>
      <time>
        {match.status === "SCHEDULED"
          ? new Date(match.startTime).toLocaleString(undefined, {
              weekday: "short",
              hour: "numeric",
              minute: "2-digit",
            })
          : new Date(match.startTime).toLocaleDateString()}
      </time>
    </article>
  );
}
