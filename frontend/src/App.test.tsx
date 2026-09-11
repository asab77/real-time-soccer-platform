import { act, render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { vi, beforeEach, test, expect } from "vitest";
import App from "./App";
import { api } from "./api/client";
vi.mock("./api/client", () => ({
  api: {
    getDemoUser: vi.fn(),
    getLeagues: vi.fn(),
    getPreferences: vi.fn(),
    getMatches: vi.fn(),
    addPreference: vi.fn(),
    removePreference: vi.fn(),
  },
}));
let socketUpdate: (message: any) => void = () => {};
vi.mock("./services/matchWebSocket", () => ({
  connectToMatchUpdates: vi.fn((_ids, onUpdate, onConnection) => {
    socketUpdate = onUpdate;
    onConnection(false);
    return () => {};
  }),
}));
const leagues = [
  { id: 1, name: "Premier League" },
  { id: 2, name: "La Liga" },
];
const live = {
  id: 10,
  leagueId: 1,
  leagueName: "Premier League",
  homeTeamId: 2,
  homeTeamName: "Arsenal",
  awayTeamId: 3,
  awayTeamName: "Chelsea",
  startTime: "2026-09-08T18:00:00Z",
  homeScore: 1,
  awayScore: 0,
  status: "LIVE" as const,
};
beforeEach(() => {
  vi.clearAllMocks();
  vi.mocked(api.getDemoUser).mockResolvedValue({ id: 7, name: "Demo User" });
  vi.mocked(api.getLeagues).mockResolvedValue(leagues);
  vi.mocked(api.getPreferences).mockResolvedValue([leagues[0]]);
  vi.mocked(api.getMatches).mockResolvedValue([live]);
  vi.mocked(api.addPreference).mockResolvedValue(undefined);
  vi.mocked(api.removePreference).mockResolvedValue(undefined);
});
test("renders leagues, selected preference and live match", async () => {
  render(<App />);
  expect(
    await screen.findByRole("button", { name: /Premier League/ }),
  ).toBeVisible();
  expect(
    screen.getByRole("button", { name: /Premier League/ }),
  ).toHaveAttribute("aria-pressed", "true");
  expect(await screen.findByText("Arsenal")).toBeVisible();
  expect(screen.getAllByText("LIVE").length).toBeGreaterThan(0);
  expect(screen.getByText("1")).toBeVisible();
  expect(screen.getByText("0")).toBeVisible();
  expect(screen.getByText("1 match · Premier League")).toBeVisible();
});
test("scheduled matches emphasize kickoff without showing a score", async () => {
  vi.mocked(api.getMatches).mockResolvedValue([
    { ...live, homeScore: null, awayScore: null, status: "SCHEDULED" },
  ]);
  render(<App />);
  expect(await screen.findByText("Kickoff")).toBeVisible();
  expect(screen.getByText("UPCOMING")).toBeVisible();
  expect(screen.queryByText("0 – 0")).not.toBeInTheDocument();
});
test("finished matches emphasize the final score", async () => {
  vi.mocked(api.getMatches).mockResolvedValue([
    { ...live, homeScore: 2, awayScore: 1, status: "FINISHED" },
  ]);
  render(<App />);
  expect(await screen.findByText("FT")).toBeVisible();
  expect(screen.getByText("2")).toBeVisible();
  expect(screen.getByText("1")).toBeVisible();
  expect(screen.getByText("Full time")).toBeVisible();
});
test("selecting and removing leagues call preference APIs", async () => {
  const user = userEvent.setup();
  render(<App />);
  await screen.findByRole("button", { name: /Premier League/ });
  await user.click(screen.getByRole("button", { name: /La Liga/ }));
  await waitFor(() => expect(api.addPreference).toHaveBeenCalledWith(7, 2));
  await user.click(screen.getByRole("button", { name: /Premier League/ }));
  await waitFor(() => expect(api.removePreference).toHaveBeenCalledWith(7, 1));
});
test("status filter requests filtered backend feed", async () => {
  const user = userEvent.setup();
  render(<App />);
  await screen.findByText("Arsenal");
  await user.click(screen.getByRole("button", { name: "Live" }));
  await waitFor(() => expect(api.getMatches).toHaveBeenCalledWith(7, "LIVE"));
});
test("websocket update refetches instead of breaking filtered state", async () => {
  render(<App />);
  await screen.findByText("Arsenal");
  act(() => socketUpdate({ ...live, homeScore: 2, updateType: "UPDATED" }));
  await waitFor(() => expect(api.getMatches).toHaveBeenCalledTimes(2));
});
test("websocket created event also refreshes the feed", async () => {
  render(<App />);
  await screen.findByText("Arsenal");
  act(() => socketUpdate({ ...live, id: 11, updateType: "CREATED" }));
  await waitFor(() => expect(api.getMatches).toHaveBeenCalledTimes(2));
});
test("REST content remains visible when websocket reports disconnected", async () => {
  render(<App />);
  expect(await screen.findByText("Arsenal")).toBeVisible();
  expect(screen.getByText("Updates via refresh")).toBeVisible();
});

test("empty filtered feed gives a useful next step", async () => {
  vi.mocked(api.getMatches).mockResolvedValue([]);
  render(<App />);
  expect(await screen.findByText("No matches in this view")).toBeVisible();
  expect(screen.getByText(/Try another filter/)).toBeVisible();
});
