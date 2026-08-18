# Expense Tracker API

[![Java](https://img.shields.io/badge/Java-17-blue.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue.svg)](https://www.postgresql.org/)

A production-style **personal finance REST API** built with Spring Boot. Track expenses, set monthly budgets, view spending summaries, export data to CSV, and stay motivated with a no-spend streak feature.

**Repository:** [github.com/Revati-Firke/Personal-Expense-Tracker](https://github.com/Revati-Firke/Personal-Expense-Tracker)

---

## Highlights

- **Layered architecture** — Controller → Service → Repository with clear module boundaries
- **PostgreSQL + Flyway** — Versioned schema migrations
- **OpenAPI / Swagger UI** — Interactive API documentation
- **Validation & error handling** — Consistent JSON error responses
- **Budget tracking** — Set limits and get over-budget warnings
- **CSV export** — Download monthly expense reports
- **No-spend streak** — Motivational metric in monthly summary
- **Docker-ready** — PostgreSQL via Docker Compose + optional app container
- **CI pipeline** — GitHub Actions runs build and tests on every push

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Database | PostgreSQL 17 |
| ORM | Spring Data JPA (Hibernate) |
| Migrations | Flyway |
| API Docs | SpringDoc OpenAPI |
| Security | Spring Security (stateless, open endpoints) |
| Testing | JUnit 5, Mockito, H2 |
| Build | Maven |

---

## Features

| Feature | Description |
|---------|-------------|
| Expense CRUD | Create, update, delete, and list expenses |
| Filtering | Filter by month and category |
| Monthly summary | Total spend, category breakdown, top category |
| Budget tracking | Set monthly budget and check status |
| CSV export | Export expenses for a selected month |
| Recurring marker | Flag subscriptions and recurring charges |
| No-spend streak | Days since last expense (motivation metric) |

---

## Quick Start

### Prerequisites

- Java 17+
- Docker (for PostgreSQL)

### 1. Clean, start PostgreSQL, rebuild, and run

```bash
./scripts/run_project.sh
```

Skip tests during the rebuild:

```bash
./scripts/run_project.sh skip-tests
```

Wipe the local database volume and start completely fresh:

```bash
./scripts/run_project.sh --reset-db
```

`./scripts/run_local.sh` is a compatibility wrapper for the same command.

### 2. Manual start (optional)

```bash
docker compose up -d
./mvnw spring-boot:run
```

### 3. Explore the API

| Resource | URL |
|----------|-----|
| API root | http://localhost:8080/ |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI JSON | http://localhost:8080/api-docs |
| Health check | http://localhost:8080/actuator/health |

---

## API Endpoints

### Expenses

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/expenses` | Create expense |
| `PUT` | `/api/v1/expenses/{id}` | Update expense |
| `DELETE` | `/api/v1/expenses/{id}` | Delete expense |
| `GET` | `/api/v1/expenses?year=2026&month=7&category=FOOD` | List expenses |
| `GET` | `/api/v1/expenses/summary?year=2026&month=7` | Monthly summary |
| `GET` | `/api/v1/expenses/export/csv?year=2026&month=7` | Export CSV |

### Budgets

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/budgets` | Set monthly budget |
| `GET` | `/api/v1/budgets/{year}/{month}` | Get budget status |

---

## Example Requests

### Create expense

```bash
curl -X POST http://localhost:8080/api/v1/expenses \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Lunch",
    "amount": 12.50,
    "category": "FOOD",
    "expenseDate": "2026-07-06",
    "recurring": false,
    "note": "Office cafeteria"
  }'
```

### Set budget

```bash
curl -X POST http://localhost:8080/api/v1/budgets \
  -H "Content-Type: application/json" \
  -d '{
    "year": 2026,
    "month": 7,
    "limitAmount": 20000
  }'
```

---

## Project Structure

```
src/main/java/com/revati/expensetracker/
├── expense/     # Expense domain, API, service, repository
├── budget/      # Monthly budget module
├── config/      # Security & OpenAPI configuration
└── shared/      # Global exception handling & common responses
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed system diagrams.

---

## Run Tests

```bash
./mvnw test
```

---

## Docker (optional)

Build and run the app container (requires PostgreSQL running):

```bash
docker build -t expense-tracker .
docker run -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/expense_tracker \
  expense-tracker
```

---

## Roadmap

- [ ] JWT authentication (multi-user support)
- [ ] Category management in database
- [ ] Recurring expense auto-generation
- [ ] Budget alert notifications
- [ ] React/Angular dashboard frontend

---

## Author

**Revati Firke** — [GitHub](https://github.com/Revati-Firke)

---

## License

Copyright (c) 2026 Revati Firke. All rights reserved. This code may not be copied, modified, or reused without permission. See [LICENSE](LICENSE).
