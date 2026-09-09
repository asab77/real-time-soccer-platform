import type { DemoUser, Filter, League, Match } from "../types";
const base = (
  import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080"
).replace(/\/$/, "");
async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(base + path, {
    ...options,
    headers: { "Content-Type": "application/json", ...options?.headers },
  });
  if (!response.ok) throw new Error(`Request failed (${response.status})`);
  return response.status === 204 ? (undefined as T) : response.json();
}
export const api = {
  getDemoUser: () => request<DemoUser>("/demo/user"),
  getLeagues: () => request<League[]>("/leagues"),
  getPreferences: (userId: number) =>
    request<League[]>(`/users/${userId}/preferences`),
  addPreference: (userId: number, leagueId: number) =>
    request(`/users/${userId}/preferences`, {
      method: "POST",
      body: JSON.stringify({ leagueId }),
    }),
  removePreference: (userId: number, leagueId: number) =>
    request(`/users/${userId}/preferences/${leagueId}`, { method: "DELETE" }),
  getMatches: (userId: number, filter: Filter) =>
    request<Match[]>(
      `/users/${userId}/matches${filter === "ALL" ? "" : `?status=${filter}`}`,
    ),
};
export const apiBaseUrl = base;
