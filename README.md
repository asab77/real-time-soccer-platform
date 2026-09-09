# Soccer Platform

A beginner-readable full-stack MVP for following soccer leagues and viewing
their matches. Spring Boot serves REST and STOMP WebSocket APIs, PostgreSQL is
the source of truth, Redis provides disposable response caching, and React
renders the browser interface.

## Architecture

```text
Browser (React)
  |-- REST + STOMP WebSocket --> Spring Boot
                                   |-- JPA --> PostgreSQL
                                   |-- cache --> Redis
                                   `-- optional sync --> API-Football
```

Flyway owns the PostgreSQL schema. Hibernate validates entity mappings but does
not create or alter production tables.

## Docker Compose quick start

Prerequisite: Docker Desktop or another Docker installation with Compose.

```bash
docker compose up --build
```

Then open the frontend at `http://localhost:3000`. The backend is available at
`http://localhost:8080`.

Compose waits for PostgreSQL and Redis health checks before starting the
backend. Flyway runs automatically against a fresh database. The idempotent demo
initializer creates the Demo User and configured leagues only when missing.

Stop while preserving PostgreSQL data:

```bash
docker compose down
```

### Environment variables

The defaults are local-development values. Override them in your shell or an
ignored `.env.compose` file:

| Variable | Default | Purpose |
| --- | --- | --- |
| `DB_NAME` | `soccer_platform` | PostgreSQL database |
| `DB_USER` | `soccer` | PostgreSQL user |
| `DB_PASSWORD` | `soccer_dev` | Local-only PostgreSQL password |
| `POSTGRES_PORT` | `5432` | Host PostgreSQL port |
| `REDIS_PORT` | `6379` | Host Redis port |
| `BACKEND_PORT` | `8080` | Host backend port |
| `FRONTEND_PORT` | `3000` | Host frontend port |
| `DEMO_DATA_ENABLED` | `true` | Create local demo data |
| `SOCCER_SYNC_ENABLED` | `false` | Enable scheduled provider sync |
| `API_FOOTBALL_KEY` | empty | Optional provider credential |

Never commit real credentials. `.env`, `.env.*`, `.env.compose`, and
`application-local.properties` are ignored.

Enable scheduled synchronization only after supplying the key outside Git:

```bash
export API_FOOTBALL_KEY=<your-key>
export SOCCER_SYNC_ENABLED=true
docker compose up --build
```

The backend starts without the key while scheduling is disabled.

### Manual fixture synchronization

One manual call consumes one provider request:

```bash
curl -X POST "http://localhost:8080/internal/sync/fixtures?externalLeagueId=39&season=2026&date=2026-09-06"
```

Only run it when a key is configured and you intend to spend a request.

### Persistence and intentional reset

PostgreSQL uses the named volume `soccer_postgres_data`; normal restarts and
`docker compose down` preserve its data. To intentionally erase the local
Compose database and rebuild it from Flyway:

```bash
docker compose down --volumes
docker compose up --build
```

The first command permanently removes the local Compose database volume.

## Running without Docker

Requirements: Java 21, PostgreSQL, Redis, and Node.js 20 or newer.

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=soccer_platform
export DB_USER=postgres
export DB_PASSWORD=your_local_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
./mvnw spring-boot:run
```

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Vite runs at `http://localhost:5173` and defaults to the backend at
`http://localhost:8080`. Override these with `VITE_API_BASE_URL` and
`FRONTEND_ORIGIN` when needed.

## Tests and builds

Backend tests use H2 and need no PostgreSQL, Redis, or provider key:

```bash
./mvnw test
```

Frontend verification:

```bash
cd frontend
npm install
npm test
npm run build
```

Automated tests mock provider behavior and never spend provider requests.

## Runtime behavior

The frontend loads initial data through REST and subscribes to selected league
topics under `/topic/leagues/{leagueId}/matches`. Match messages cause a REST
refetch of the active filter. If Redis is unavailable, cache failures are logged
and PostgreSQL-backed reads continue. If WebSocket connectivity is lost,
existing REST content stays visible while the client retries.
