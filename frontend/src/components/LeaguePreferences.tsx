import type { League } from "../types";

const leagueAbbreviations: Record<string, string> = {
  "Premier League": "PL",
  "La Liga": "LL",
  Bundesliga: "BL",
  "Serie A": "SA",
  "Ligue 1": "L1",
};

function abbreviation(name: string) {
  return leagueAbbreviations[name] ?? name.slice(0, 2).toUpperCase();
}

export function LeaguePreferences({
  leagues,
  selected,
  busy,
  onToggle,
}: {
  leagues: League[];
  selected: Set<number>;
  busy: boolean;
  onToggle: (league: League) => void;
}) {
  return (
    <section className="panel">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Your competitions</p>
          <h2>Favorite leagues</h2>
        </div>
        <span>{selected.size} selected</span>
      </div>
      <div className="league-grid">
        {leagues.map((l) => (
          <button
            disabled={busy}
            aria-pressed={selected.has(l.id)}
            className={`league ${selected.has(l.id) ? "selected" : ""}`}
            key={l.id}
            onClick={() => onToggle(l)}
          >
            <span className="league-badge" aria-hidden="true">
              {abbreviation(l.name)}
            </span>
            <span className="league-name">{l.name}</span>
            <span className="league-action" aria-hidden="true">
              {selected.has(l.id) ? "Selected ✓" : "Select +"}
            </span>
          </button>
        ))}
      </div>
    </section>
  );
}
