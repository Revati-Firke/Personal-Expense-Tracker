#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080/api/v1"

printf "\n=== Create expense ===\n"
curl -s -X POST "$BASE_URL/expenses" \
  -H "Content-Type: application/json" \
  -d '{"description":"Grocery","amount":50.25,"category":"FOOD","expenseDate":"2024-08-01","recurring":false,"note":"weekly groceries"}' \
  -w "\nHTTP: %{http_code}\n" \
  || true

printf "\n=== List expenses (month=8) ===\n"
curl -s "$BASE_URL/expenses?month=8" -w "\nHTTP: %{http_code}\n" || true

printf "\n=== Create budget ===\n"
curl -s -X POST "$BASE_URL/budgets" \
  -H "Content-Type: application/json" \
  -d '{"year":2024,"month":8,"limitAmount":1000.00}' \
  -w "\nHTTP: %{http_code}\n" || true

printf "\n=== Get budget status ===\n"
curl -s "$BASE_URL/budgets/2024/8" -w "\nHTTP: %{http_code}\n" || true

printf "\n=== Update expense (replace ID=1 as needed) ===\n"
curl -s -X PUT "$BASE_URL/expenses/1" \
  -H "Content-Type: application/json" \
  -d '{"description":"Grocery - Updated","amount":60.00,"category":"FOOD","expenseDate":"2024-08-02","recurring":false,"note":"updated"}' \
  -w "\nHTTP: %{http_code}\n" || true

printf "\n=== Delete expense (replace ID=1 as needed) ===\n"
curl -s -X DELETE "$BASE_URL/expenses/1" -w "\nHTTP: %{http_code}\n" || true

printf "\nNote: Update IDs in update/delete calls to match created resource IDs.\n"
