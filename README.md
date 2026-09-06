# Soccer Platform Backend

Initial Spring Boot foundation for a soccer match platform.

## Requirements

- Java 21
- PostgreSQL

## Local database configuration

The application defaults to a local database named `soccer_platform` with the
username and password `postgres`. Override those values when needed:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/soccer_platform
export DB_USERNAME=postgres
export DB_PASSWORD=your_password
```

## Run

```bash
./mvnw test
./mvnw spring-boot:run
```

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
