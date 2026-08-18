#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

SKIP_TESTS=false
RESET_DB=false
CONTAINER_NAME="expense-tracker-postgres"

usage() {
  cat <<'EOF'
Usage: ./scripts/run_project.sh [options]

Clean compiled files, start PostgreSQL, rebuild, and run the app.

Options:
  skip-tests, --skip-tests   Skip Maven tests during the rebuild
  --reset-db                 Recreate PostgreSQL and wipe local database volume
  -h, --help                 Show this help

Examples:
  ./scripts/run_project.sh
  ./scripts/run_project.sh skip-tests
  ./scripts/run_project.sh --skip-tests --reset-db
EOF
}

for arg in "$@"; do
  case "$arg" in
    skip-tests|--skip-tests)
      SKIP_TESTS=true
      ;;
    --reset-db)
      RESET_DB=true
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown option: $arg" >&2
      usage
      exit 1
      ;;
  esac
done

compose() {
  if docker compose version >/dev/null 2>&1; then
    docker compose "$@"
  elif command -v docker-compose >/dev/null 2>&1; then
    docker-compose "$@"
  else
    echo "Docker Compose is required. Install Docker Desktop or docker-compose." >&2
    exit 1
  fi
}

echo "==> Project root: $ROOT_DIR"

echo
echo "==> 1) Stop anything already using port 8080"
if command -v fuser >/dev/null 2>&1 && fuser 8080/tcp >/dev/null 2>&1; then
  fuser -k 8080/tcp >/dev/null 2>&1 || true
  sleep 1
  echo "    Stopped process on port 8080"
else
  echo "    Port 8080 is free"
fi

echo
echo "==> 2) Stop and remove old PostgreSQL container"
compose down --remove-orphans >/dev/null 2>&1 || true
if docker ps -a --format '{{.Names}}' | grep -qx "$CONTAINER_NAME"; then
  docker rm -f "$CONTAINER_NAME" >/dev/null
  echo "    Removed leftover container: $CONTAINER_NAME"
fi
if [ "$RESET_DB" = true ]; then
  compose down -v --remove-orphans >/dev/null 2>&1 || true
  echo "    Database volume removed (--reset-db)"
fi

echo
echo "==> 3) Clean compiled Maven files"
./mvnw -q clean
echo "    target/ removed"

echo
echo "==> 4) Start PostgreSQL"
compose up -d

echo
echo "==> 5) Wait for PostgreSQL to accept connections"
ready=false
for i in $(seq 1 30); do
  if docker exec "$CONTAINER_NAME" pg_isready -U postgres >/dev/null 2>&1; then
    ready=true
    break
  fi
  sleep 1
done
if [ "$ready" != true ]; then
  echo "PostgreSQL did not become ready on localhost:5432." >&2
  docker logs "$CONTAINER_NAME" >&2 || true
  exit 1
fi
echo "    PostgreSQL is ready"

echo
echo "==> 6) Rebuild the project"
if [ "$SKIP_TESTS" = true ]; then
  ./mvnw clean package -DskipTests
else
  ./mvnw clean package
fi

echo
echo "==> 7) Run the application (Ctrl+C to stop)"
exec ./mvnw spring-boot:run
