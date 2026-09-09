# Soccer Platform

A beginner-readable full-stack MVP for following soccer leagues and seeing their
matches. The Spring Boot backend owns persistence and provider synchronization;
the React frontend uses REST for its initial state and WebSockets for update
notifications.

## Requirements

- Java 21
- PostgreSQL
- Redis
- Node.js 20 or newer

## Local database configuration

The application defaults to a local database named `soccer_platform` with the
username and password `postgres`. Override those values when needed:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/soccer_platform
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

## Local Redis configuration

Redis caches match response DTOs for 45 seconds by default. PostgreSQL remains
the source of truth. Start a local Redis container with:

```bash
docker run --name soccer-redis -p 6379:6379 -d redis:7-alpine
```

If Docker is unavailable on macOS, install Redis yourself with Homebrew and run
`brew services start redis`. The application accepts these overrides:

```bash
export REDIS_HOST=localhost
export REDIS_PORT=6379
export REDIS_PASSWORD=your_password
export MATCH_CACHE_TTL=45s
```

Do not set `REDIS_PASSWORD` when the local Redis server has no password.

## Run

```bash
./mvnw test
./mvnw spring-boot:run
```

The backend creates an idempotent local demo user and a small league catalog by
default. Disable this outside local demos with `DEMO_DATA_ENABLED=false`.

## Frontend

In a second terminal:

```bash
cd frontend
npm install
npm test
npm run dev
```

Open `http://localhost:5173`. The frontend defaults to a backend at
`http://localhost:8080`. Override it when needed:

```bash
export VITE_API_BASE_URL=http://localhost:8080
npm run dev
```

The backend allows the local Vite origin by default. For a different frontend
origin, set `FRONTEND_ORIGIN` before starting Spring Boot:

```bash
export FRONTEND_ORIGIN=http://localhost:5173
```

## Demo flow

1. Start PostgreSQL and Redis.
2. Start the Spring Boot backend.
3. Start the Vite frontend.
4. Select or remove leagues in the preferences panel.
5. Use the All, Live, Scheduled, and Finished match filters.

The page loads data through REST. It subscribes only to the selected league
topics under `/topic/leagues/{leagueId}/matches`. When a match-created or
match-updated message arrives, it refetches the current filtered REST feed so
the server remains the source of truth. If WebSocket connectivity is lost, the
existing REST content stays available and the client retries automatically.

## API-Football configuration

Set the API key through an environment variable before manually synchronizing
fixtures:

```bash
export API_FOOTBALL_KEY=<your-api-key>
```

Never commit the API key or a local secrets file. Files named `.env`, `.env.*`,
and `application-local.properties` are ignored by Git.

One manual synchronization fetches fixtures for one configured league on one
date and consumes one API-Football request:

```bash
curl -X POST "http://localhost:8080/internal/sync/fixtures?externalLeagueId=39&season=2026&date=2026-09-06"
```

## Demonstrating match caching locally

1. Start PostgreSQL, Redis, and the application.
2. Run the same request twice:

```bash
curl "http://localhost:8080/leagues/1/matches?status=LIVE"
curl "http://localhost:8080/leagues/1/matches?status=LIVE"
```

The first request is a cache miss and reads PostgreSQL. The second request is a
cache hit until the TTL expires. Inspect the key without changing application
behavior:

```bash
redis-cli KEYS 'leagueMatches::*'
```

To demonstrate automatic invalidation, run one fixture synchronization. After
the database transaction commits, both match cache regions are cleared. The next
match request reads the updated PostgreSQL data and repopulates Redis.
