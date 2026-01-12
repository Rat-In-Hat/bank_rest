# Bank Cards Management Service

Backend service for managing bank cards, balances, and user access with Spring Boot, JWT, and PostgreSQL.

## Prerequisites

- Java 17+
- Docker + Docker Compose (for the PostgreSQL dev database)
- (Optional) `curl` for fetching OpenAPI specs

## Database (Docker Compose)

Start the development database:

```bash
docker compose up -d
```

This provisions PostgreSQL with:

- **Host:** `localhost:5432`
- **Database:** `bankcards`
- **User:** `bankcards`
- **Password:** `bankcards`

To stop and remove containers:

```bash
docker compose down
```

## Running the Application

From the repository root:

```bash
./mvnw spring-boot:run
```

The application uses the `dev` profile by default (`src/main/resources/application.yml`) and connects to the Docker Compose database using `src/main/resources/application-dev.yml`.

## Tests

Run the test suite with:

```bash
./mvnw test
```

## Admin User (Creation & Credentials)

An admin account is seeded via Liquibase during startup in the change set:

- `src/main/resources/db/migration/004-seed-admin-user.xml`

The change set inserts an admin user only if one does **not** already exist.

**Default admin credentials** (created automatically by Liquibase):

- **Username:** `admin`
- **Password:** `admin`

If you need different credentials, update the bcrypt hash in the same change set and re-run migrations on a fresh database.

## Swagger UI & OpenAPI

- **Swagger UI:** `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON:** `http://localhost:8080/v3/api-docs`
- **OpenAPI YAML:** `http://localhost:8080/v3/api-docs.yaml`

The repository’s OpenAPI snapshot lives here:

- `docs/openapi.yaml`

### Regenerating `docs/openapi.yaml`

1. Start the application:

```bash
./mvnw spring-boot:run
```

2. Fetch the OpenAPI YAML:

```bash
curl -o docs/openapi.yaml http://localhost:8080/v3/api-docs.yaml
```
