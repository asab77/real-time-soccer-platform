import { useCallback, useEffect, useMemo, useState } from "react";
import { api } from "./api/client";
import { LeaguePreferences } from "./components/LeaguePreferences";
import { MatchDashboard } from "./components/MatchDashboard";
import { connectToMatchUpdates } from "./services/matchWebSocket";
import type { DemoUser, Filter, League, Match } from "./types";
import "./styles.css";
export default function App() {
  const [user, setUser] = useState<DemoUser>();
  const [leagues, setLeagues] = useState<League[]>([]);
  const [prefs, setPrefs] = useState<League[]>([]);
  const [matches, setMatches] = useState<Match[]>([]);
  const [filter, setFilter] = useState<Filter>("ALL");
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [connected, setConnected] = useState(false);
  const selected = useMemo(() => new Set(prefs.map((p) => p.id)), [prefs]);
  const loadMatches = useCallback(async (id: number, f: Filter) => {
    setLoading(true);
    try {
      setMatches(await api.getMatches(id, f));
      setError("");
    } catch {
      setError("The match service is unavailable. Please try again.");
    } finally {
      setLoading(false);
    }
  }, []);
  useEffect(() => {
    void (async () => {
      try {
        const [u, l] = await Promise.all([api.getDemoUser(), api.getLeagues()]);
        setUser(u);
        setLeagues(l);
        const p = await api.getPreferences(u.id);
        setPrefs(p);
      } catch {
        setError("Unable to load the soccer platform. Is the backend running?");
        setLoading(false);
      }
    })();
  }, []);
  useEffect(() => {
    if (!user) return;
    void loadMatches(user.id, filter);
  }, [filter, user, loadMatches]);
  useEffect(() => {
    if (!user || !prefs.length) return;
    return connectToMatchUpdates(
      prefs.map((p) => p.id),
      () => void loadMatches(user.id, filter),
      setConnected,
    );
  }, [user, prefs, filter, loadMatches]);
  async function toggle(l: League) {
    if (!user) return;
    setBusy(true);
    try {
      if (selected.has(l.id)) {
        await api.removePreference(user.id, l.id);
        setPrefs((p) => p.filter((x) => x.id !== l.id));
      } else {
        await api.addPreference(user.id, l.id);
        setPrefs((p) => [...p, l]);
      }
      await loadMatches(user.id, filter);
    } catch {
      setError("Could not save that preference. Please try again.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <main>
      <header>
        <div>
          <p className="brand">
            PITCH<span>LIVE</span>
          </p>
          <h1>Every match that matters.</h1>
          <p>Follow your leagues. Track every kickoff. See scores move live.</p>
        </div>
        <div className={`connection ${connected ? "online" : ""}`}>
          {connected ? "Live updates on" : "REST mode"}
        </div>
      </header>
      {error && <div className="error">{error}</div>}
      <LeaguePreferences
        leagues={leagues}
        selected={selected}
        busy={busy}
        onToggle={toggle}
      />
      {!prefs.length && !loading ? (
        <div className="state onboarding">
          Choose at least one league to build your match feed.
        </div>
      ) : (
        <MatchDashboard
          matches={matches}
          filter={filter}
          onFilter={setFilter}
          loading={loading}
        />
      )}
    </main>
  );
}
