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

There are no HTTP endpoints yet. A successful application start confirms that
Spring can connect to PostgreSQL and initialize the current entity schema.
