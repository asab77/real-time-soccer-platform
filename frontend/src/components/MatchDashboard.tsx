import type { Filter, Match } from "../types";
import { MatchCard } from "./MatchCard";
const filters: Filter[] = ["ALL", "LIVE", "SCHEDULED", "FINISHED"];
export function MatchDashboard({
  matches,
  filter,
  onFilter,
  loading,
}: {
  matches: Match[];
  filter: Filter;
  onFilter: (f: Filter) => void;
  loading: boolean;
}) {
  return (
    <section className="feed">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Match center</p>
          <h2>Your fixtures</h2>
        </div>
        <div className="filters">
          {filters.map((f) => (
            <button
              className={filter === f ? "active" : ""}
              onClick={() => onFilter(f)}
              key={f}
            >
              {f[0] + f.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
      </div>
      {loading ? (
        <div className="state">Loading matches…</div>
      ) : matches.length ? (
        <div className="match-list">
          {matches.map((m) => (
            <MatchCard key={m.id ?? m.matchId} match={m} />
          ))}
        </div>
      ) : (
        <div className="state">No matches found for this view.</div>
      )}
    </section>
  );
}
