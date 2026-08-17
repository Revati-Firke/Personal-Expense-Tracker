CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(160) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    category VARCHAR(30) NOT NULL,
    expense_date DATE NOT NULL,
    recurring BOOLEAN NOT NULL DEFAULT FALSE,
    note VARCHAR(180),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_expenses_date ON expenses(expense_date);
CREATE INDEX idx_expenses_category ON expenses(category);

CREATE TABLE monthly_budgets (
    id BIGSERIAL PRIMARY KEY,
    budget_year INT NOT NULL,
    budget_month INT NOT NULL,
    limit_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_budget_year_month UNIQUE (budget_year, budget_month)
);
