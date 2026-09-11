import type { Match } from "../types";
const labels = {
  LIVE: "LIVE",
  FINISHED: "FT",
  SCHEDULED: "UPCOMING",
  POSTPONED: "POSTPONED",
  UNKNOWN: "STATUS PENDING",
};

function teamAbbreviation(name: string) {
  const words = name.trim().split(/\s+/);
  if (words.length === 1) return words[0].slice(0, 3).toUpperCase();
  return words.slice(0, 3).map((word) => word[0]).join("").toUpperCase();
}

export function MatchCard({ match }: { match: Match }) {
  const scores = match.homeScore != null && match.awayScore != null;
  const kickoff = new Date(match.startTime);
  const scheduled = match.status === "SCHEDULED";
  const scoreStatus = match.status === "LIVE" || match.status === "FINISHED";

  return (
    <article className={`match-card ${match.status.toLowerCase()}`}>
      <div className="match-meta">
        <span>{match.leagueName ?? "League"}</span>
        <strong>{labels[match.status]}</strong>
      </div>
      <div className="match-body">
        <div className="team-row">
          <span className="team-badge" aria-hidden="true">{teamAbbreviation(match.homeTeamName)}</span>
          <span className="team-name">{match.homeTeamName}</span>
          {scoreStatus && <b className="team-score">{scores ? match.homeScore : "–"}</b>}
        </div>
        <div className="team-row">
          <span className="team-badge" aria-hidden="true">{teamAbbreviation(match.awayTeamName)}</span>
          <span className="team-name">{match.awayTeamName}</span>
          {scoreStatus && <b className="team-score">{scores ? match.awayScore : "–"}</b>}
        </div>
      </div>
      {scheduled ? (
        <div className="kickoff">
          <span>Kickoff</span>
          <time dateTime={match.startTime}>
            {kickoff.toLocaleString(undefined, {
              weekday: "short",
              month: "short",
              day: "numeric",
              hour: "numeric",
              minute: "2-digit",
            })}
          </time>
        </div>
      ) : (
        <div className="match-footer">
          <span>
            {match.status === "LIVE" && "Score updates automatically"}
            {match.status === "FINISHED" && "Full time"}
            {match.status === "POSTPONED" && "A new kickoff time is pending"}
            {match.status === "UNKNOWN" && "Match status is not yet available"}
          </span>
          <time dateTime={match.startTime}>{kickoff.toLocaleDateString()}</time>
        </div>
      )}
    </article>
  );
}
