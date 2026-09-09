import type { League } from "../types";
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
            <span className="ball">●</span>
            {l.name}
            <span>{selected.has(l.id) ? "✓" : "+"}</span>
          </button>
        ))}
      </div>
    </section>
  );
}
