#!/usr/bin/env bash
set -euo pipefail

BASE_URL="http://localhost:8080/api/v1"
YEAR=2024
MONTH=8
CREATE_DATE="$(printf '%d-%02d-01' "$YEAR" "$MONTH")"
UPDATE_DATE="$(printf '%d-%02d-02' "$YEAR" "$MONTH")"

printf "\n=== Create expense ===\n"
CREATE_RESPONSE="$(curl -s -w "\nHTTP: %{http_code}" -X POST "$BASE_URL/expenses" \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"Grocery\",\"amount\":50.25,\"category\":\"FOOD\",\"expenseDate\":\"${CREATE_DATE}\",\"recurring\":false,\"note\":\"weekly groceries\"}")"
printf '%s\n' "$CREATE_RESPONSE"

CREATE_BODY="$(printf '%s\n' "$CREATE_RESPONSE" | sed '$d')"
EXPENSE_ID="$(python3 -c "import json,sys; print(json.loads(sys.argv[1])['id'])" "$CREATE_BODY")"

printf "\n=== List expenses (year=%s&month=%s) ===\n" "$YEAR" "$MONTH"
curl -s "$BASE_URL/expenses?year=${YEAR}&month=${MONTH}" -w "\nHTTP: %{http_code}\n"

printf "\n=== Create budget ===\n"
curl -s -X POST "$BASE_URL/budgets" \
  -H "Content-Type: application/json" \
  -d "{\"year\":${YEAR},\"month\":${MONTH},\"limitAmount\":1000.00}" \
  -w "\nHTTP: %{http_code}\n"

printf "\n=== Get budget status ===\n"
curl -s "$BASE_URL/budgets/${YEAR}/${MONTH}" -w "\nHTTP: %{http_code}\n"

printf "\n=== Update expense (id=%s) ===\n" "$EXPENSE_ID"
curl -s -X PUT "$BASE_URL/expenses/${EXPENSE_ID}" \
  -H "Content-Type: application/json" \
  -d "{\"description\":\"Grocery - Updated\",\"amount\":60.00,\"category\":\"FOOD\",\"expenseDate\":\"${UPDATE_DATE}\",\"recurring\":false,\"note\":\"updated\"}" \
  -w "\nHTTP: %{http_code}\n"

printf "\n=== Delete expense (id=%s) ===\n" "$EXPENSE_ID"
curl -s -X DELETE "$BASE_URL/expenses/${EXPENSE_ID}" -w "\nHTTP: %{http_code}\n"
