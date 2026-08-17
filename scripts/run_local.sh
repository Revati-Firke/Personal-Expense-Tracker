#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

echo "1) Build the project (skip tests if desired)"
if [ "$1" = "skip-tests" ] 2>/dev/null; then
  ./mvnw clean package -DskipTests
else
  ./mvnw clean package
fi

echo
echo "2) Run the application with Maven (in this terminal)"
echo "Use Ctrl+C to stop."
./mvnw spring-boot:run

# Alternative: run with docker-compose (if you prefer)
# docker-compose up --build
