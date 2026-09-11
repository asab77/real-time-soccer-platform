import type { Filter, League, Match } from "../types";
import { MatchCard } from "./MatchCard";
const filters: Filter[] = ["ALL", "LIVE", "SCHEDULED", "FINISHED"];
export function MatchDashboard({
  matches,
  followedLeagues,
  filter,
  onFilter,
  loading,
}: {
  matches: Match[];
  followedLeagues: League[];
  filter: Filter;
  onFilter: (f: Filter) => void;
  loading: boolean;
}) {
  const competitionSummary =
    followedLeagues.length === 1
      ? followedLeagues[0].name
      : `${followedLeagues.length} competitions`;

  return (
    <section className="feed">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Match center</p>
          <h2>Your fixtures</h2>
          <p className="feed-summary">
            {loading ? "Updating fixtures…" : `${matches.length} ${matches.length === 1 ? "match" : "matches"} · ${competitionSummary}`}
          </p>
        </div>
        <div className="filters">
          {filters.map((f) => (
            <button
              className={filter === f ? "active" : ""}
              onClick={() => onFilter(f)}
              key={f}
              aria-pressed={filter === f}
            >
              {f[0] + f.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>
      {loading ? (
        <div className="match-list skeleton-list" aria-label="Loading matches">
          <div className="match-card skeleton" />
          <div className="match-card skeleton" />
        </div>
      ) : matches.length ? (
        <div className="match-list">
          {matches.map((m) => (
            <MatchCard key={m.id ?? m.matchId} match={m} />
          ))}
        </div>
      ) : (
        <div className="state empty-state">
          <strong>No matches in this view</strong>
          <span>Try another filter or check back when fixtures are available.</span>
        </div>
      )}
    </section>
  );
}
