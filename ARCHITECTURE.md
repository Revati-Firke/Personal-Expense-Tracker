# Expense Tracker - Complete Architecture Diagram

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENT LAYER                              │
│  (Web Browser / Mobile App / REST Client)                           │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ HTTP/REST Requests
             │ (JSON Payloads)
             │
┌────────────▼────────────────────────────────────────────────────────┐
│                       API GATEWAY / LOAD BALANCER                   │
│                    (Spring Boot Embedded Server)                    │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ Route requests to Controllers
             │
┌────────────▼────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER (API)                         │
│  ┌──────────────────────┐  ┌──────────────────────┐                │
│  │ ExpenseController    │  │ BudgetController     │                │
│  │ - POST /create       │  │ - POST /setBudget    │                │
│  │ - PUT /update        │  │ - GET /getStatus     │                │
│  │ - DELETE /delete     │  │                      │                │
│  │ - GET /list          │  │                      │                │
│  └──────────────────────┘  └──────────────────────┘                │
│         ↓                           ↓                               │
│  DTO/Request Models         DTO/Response Models                   │
│  DTO/Response Models        (with validation)                     │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ Delegate business logic
             │
┌────────────▼────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER (SERVICE)                   │
│  ┌──────────────────────┐  ┌──────────────────────┐                │
│  │ ExpenseService       │  │ BudgetService        │                │
│  │ - create()           │  │ - setBudget()        │                │
│  │ - update()           │  │ - getStatus()        │                │
│  │ - delete()           │  │ - calculateSpent()   │                │
│  │ - list()             │  │ - getRemaining()     │                │
│  │ - export()           │  │ - isOverBudget()     │                │
│  │ - validateAmount()   │  │                      │                │
│  │ - buildStats()       │  │                      │                │
│  └──────────────────────┘  └──────────────────────┘                │
│         ↓                           ↓                               │
│  Validation & Transformation    Business Rules Enforcement         │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ Persistence operations
             │
┌────────────▼────────────────────────────────────────────────────────┐
│                 DATA ACCESS LAYER (REPOSITORY)                      │
│  ┌──────────────────────┐  ┌──────────────────────┐                │
│  │ ExpenseRepository    │  │ MonthlyBudgetRepo    │                │
│  │ extends              │  │ extends              │                │
│  │ JpaRepository        │  │ JpaRepository        │                │
│  │                      │  │                      │                │
│  │ - findByCategory()   │  │ - findByYearMonth()  │                │
│  │ - findByExpenseDate()│  │                      │                │
│  │ - custom queries     │  │                      │                │
│  └──────────────────────┘  └──────────────────────┘                │
└────────────┬────────────────────────────────────────────────────────┘
             │
             │ SQL Queries (JPA/Hibernate ORM)
             │
┌────────────▼────────────────────────────────────────────────────────┐
│                   PERSISTENCE LAYER (DATABASE)                      │
│                        PostgreSQL Database                          │
│  ┌─────────────────────────┐  ┌──────────────────────┐             │
│  │   expenses table        │  │ monthly_budgets tbl  │             │
│  │ ├─ id (PK)              │  │ ├─ id (PK)           │             │
│  │ ├─ description          │  │ ├─ budget_year       │             │
│  │ ├─ amount               │  │ ├─ budget_month      │             │
│  │ ├─ category (ENUM)      │  │ ├─ limit_amount      │             │
│  │ ├─ expense_date         │  │ ├─ created_at        │             │
│  │ ├─ recurring (BOOLEAN)  │  │ ├─ updated_at        │             │
│  │ ├─ note                 │  │ └─ UNIQUE(yr, mon)   │             │
│  │ ├─ created_at           │  │                      │             │
│  │ └─ updated_at           │  │                      │             │
│  │                         │  │                      │             │
│  │ Indexes:               │  │                      │             │
│  │ - idx_expenses_date    │  │                      │             │
│  │ - idx_expenses_categ   │  │                      │             │
│  └─────────────────────────┘  └──────────────────────┘             │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 2. Layered Architecture Breakdown

### Layer 1: Presentation Layer (API Controllers)
```
┌──────────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                                │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  HTTP Requests → RestController → Validation → Response DTOs        │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ExpenseController (/api/v1/expenses)                        │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ POST   /api/v1/expenses              → create()            │   │
│  │ PUT    /api/v1/expenses/{id}         → update()            │   │
│  │ DELETE /api/v1/expenses/{id}         → delete()            │   │
│  │ GET    /api/v1/expenses?month=X      → list()              │   │
│  │        &category=FOOD                                      │   │
│  │ GET    /api/v1/expenses/{id}/export  → export()            │   │
│  │ GET    /api/v1/expenses/stats        → getStats()          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ BudgetController (/api/v1/budgets)                          │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ POST GET /api/v1/budgets              → setBudget()        │   │
│  │ GET     /api/v1/budgets/{year}/{mon}  → getStatus()        │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  Features:                                                            │
│  • @RestController - Marks as REST endpoint                         │
│  • @RequestMapping - Base URL routing                               │
│  • @Valid - Bean validation on request bodies                       │
│  • ResponseEntity<T> - Structured HTTP responses                    │
│  • Exception handling via GlobalExceptionHandler                    │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### Layer 2: Business Logic Layer (Services)
```
┌──────────────────────────────────────────────────────────────────────┐
│                  BUSINESS LOGIC LAYER (SERVICE)                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ExpenseService                                              │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ @Service                                                    │   │
│  │ @RequiredArgsConstructor                                    │   │
│  │ @Transactional                                              │   │
│  │                                                              │   │
│  │ Public Methods:                                             │   │
│  │ • create(CreateExpenseRequest) → ExpenseResponse           │   │
│  │ • update(id, UpdateExpenseRequest) → ExpenseResponse       │   │
│  │ • delete(id) → void                                        │   │
│  │ • list(month, category) → List<ExpenseResponse>           │   │
│  │ • exportToCsv(month, category) → String                    │   │
│  │ • getStats(month) → StatisticsResponse                     │   │
│  │                                                              │   │
│  │ Private Methods (Validation & Helper):                      │   │
│  │ • validateAmount(BigDecimal)                               │   │
│  │ • buildCategoryStats() → Map<Category, Total>             │   │
│  │ • toResponse(Expense) → ExpenseResponse                    │   │
│  │                                                              │   │
│  │ Injected Dependencies:                                      │   │
│  │ → ExpenseRepository                                        │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ BudgetService                                               │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ @Service                                                    │   │
│  │ @RequiredArgsConstructor                                    │   │
│  │                                                              │   │
│  │ Public Methods:                                             │   │
│  │ • setBudget(SetBudgetRequest) → BudgetStatusResponse       │   │
│  │ • getStatus(year, month) → BudgetStatusResponse            │   │
│  │                                                              │   │
│  │ Private Methods:                                            │   │
│  │ • calculateSpentAmount(year, month) → BigDecimal           │   │
│  │ • getRemainingAmount() → BigDecimal                        │   │
│  │ • isOverBudget() → boolean                                 │   │
│  │ • toResponse(budget, spent) → BudgetStatusResponse         │   │
│  │                                                              │   │
│  │ Injected Dependencies:                                      │   │
│  │ → MonthlyBudgetRepository                                  │   │
│  │ → ExpenseRepository                                        │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  Key Features:                                                        │
│  ✓ @Transactional ensures ACID compliance                          │
│  ✓ Business logic validation & transformation                      │
│  ✓ Exception handling (NotFoundException, BadRequestException)     │
│  ✓ Dependency injection via @RequiredArgsConstructor               │
│  ✓ Clear separation of concerns                                    │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### Layer 3: Data Access Layer (Repositories)
```
┌──────────────────────────────────────────────────────────────────────┐
│              DATA ACCESS LAYER (REPOSITORY/DAO)                      │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ ExpenseRepository extends JpaRepository<Expense, Long>      │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ Inherited from JpaRepository:                               │   │
│  │ • save(Entity) → Entity                                    │   │
│  │ • update(Entity) → Entity                                  │   │
│  │ • delete(Entity) → void                                    │   │
│  │ • findById(id) → Optional<Entity>                          │   │
│  │ • findAll() → List<Entity>                                 │   │
│  │ • deleteById(id) → void                                    │   │
│  │                                                              │   │
│  │ Custom Query Methods:                                       │   │
│  │ • findByCategory(CategoryType) → List<Expense>             │   │
│  │ • findByExpenseDateBetween(...) → List<Expense>            │   │
│  │ • findByRecurringTrue() → List<Expense>                    │   │
│  │                                                              │   │
│  │ @Query Annotations for Complex Queries:                     │   │
│  │ • sumByCategory(category) → BigDecimal                     │   │
│  │ • findByMonthAndCategory(month) → List<Expense>            │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ MonthlyBudgetRepository extends                             │   │
│  │ JpaRepository<MonthlyBudget, Long>                          │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ Inherited Methods: (same as ExpenseRepository)              │   │
│  │                                                              │   │
│  │ Custom Query Methods:                                       │   │
│  │ • findByYearAndMonth(int, int) → Optional<MonthlyBudget>   │   │
│  │ • findByYear(int) → List<MonthlyBudget>                    │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  Key Features:                                                        │
│  ✓ CRUD operations automatically provided by JpaRepository          │
│  ✓ Spring Data JPA handles SQL generation                           │
│  ✓ Custom queries defined via method naming convention              │
│  ✓ @Query for complex JPQL/SQL queries                              │
│  ✓ Lazy loading & transaction management                            │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### Layer 4: Domain Model Layer (Entities)
```
┌──────────────────────────────────────────────────────────────────────┐
│              DOMAIN MODEL LAYER (JPA ENTITIES)                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ Expense Entity (@Entity @Table("expenses"))                │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ Attributes:                                                  │   │
│  │ • id: Long (@Id @GeneratedValue)                           │   │
│  │ • description: String (VARCHAR 160)                        │   │
│  │ • amount: BigDecimal (NUMERIC 12,2)                        │   │
│  │ • category: CategoryType (@Enumerated)                     │   │
│  │ • expenseDate: LocalDate (DATE)                            │   │
│  │ • recurring: boolean (BOOLEAN)                             │   │
│  │ • note: String (VARCHAR 180)                               │   │
│  │ • createdAt: LocalDateTime (TIMESTAMP, updatable=false)    │   │
│  │ • updatedAt: LocalDateTime (TIMESTAMP)                     │   │
│  │                                                              │   │
│  │ Annotations:                                                │   │
│  │ • @Getter @Setter (Lombok)                                 │   │
│  │ • @NoArgsConstructor @AllArgsConstructor (Lombok)          │   │
│  │ • @Builder (Lombok - for builder pattern)                  │   │
│  │ • @PrePersist (audit timestamps)                           │   │
│  │                                                              │   │
│  │ Indexes:                                                     │   │
│  │ • idx_expenses_date (for fast date queries)                │   │
│  │ • idx_expenses_category (for category filters)             │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ MonthlyBudget Entity (@Entity @Table("monthly_budgets"))   │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ Attributes:                                                  │   │
│  │ • id: Long (@Id @GeneratedValue)                           │   │
│  │ • year: Integer (INT)                                      │   │
│  │ • month: Integer (INT)                                     │   │
│  │ • limitAmount: BigDecimal (NUMERIC 12,2)                   │   │
│  │ • createdAt: LocalDateTime (TIMESTAMP, updatable=false)    │   │
│  │ • updatedAt: LocalDateTime (TIMESTAMP)                     │   │
│  │                                                              │   │
│  │ Annotations:                                                │   │
│  │ • @Getter @Setter (Lombok)                                 │   │
│  │ • @NoArgsConstructor @AllArgsConstructor (Lombok)          │   │
│  │ • @Builder (Lombok)                                        │   │
│  │ • @UniqueConstraint (year + month must be unique)          │   │
│  │ • @PrePersist (audit timestamps)                           │   │
│  │                                                              │   │
│  │ Constraints:                                                 │   │
│  │ • UNIQUE(budget_year, budget_month)                        │   │
│  │   → Only one budget per month                              │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ CategoryType Enum                                           │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │ Values:                                                      │   │
│  │ • FOOD        → Groceries & dining                         │   │
│  │ • TRANSPORT   → Vehicle & travel costs                     │   │
│  │ • UTILITIES   → Electricity, water, gas                    │   │
│  │ • ENTERTAINMENT → Movies, games, hobbies                   │   │
│  │ • HEALTH      → Medical & fitness                          │   │
│  │ • SHOPPING    → Clothes, accessories                       │   │
│  │ • OTHER       → Miscellaneous expenses                     │   │
│  │                                                              │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

### Layer 5: Database Layer
```
┌──────────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (PostgreSQL)                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  Connection Pool: HikariCP (via Spring Boot)                        │
│  │                                                                   │
│  ├─ Server: PostgreSQL                                             │
│  ├─ Port: 5432 (default)                                           │
│  ├─ Version: 14+                                                   │
│  │                                                                   │
│  │                                                                   │
│  ├─────────────────────────────────────────────────────────────┐   │
│  │ TABLE: expenses                                             │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ COLUMN            │ TYPE          │ CONSTRAINTS             │   │
│  ├───────────────────┼───────────────┼─────────────────────────┤   │
│  │ id                │ BIGSERIAL     │ PRIMARY KEY             │   │
│  │ description       │ VARCHAR(160)  │ NOT NULL                │   │
│  │ amount            │ NUMERIC(12,2) │ NOT NULL                │   │
│  │ category          │ VARCHAR(30)   │ NOT NULL (ENUM)         │   │
│  │ expense_date      │ DATE          │ NOT NULL                │   │
│  │ recurring         │ BOOLEAN       │ NOT NULL, DEFAULT FALSE │   │
│  │ note              │ VARCHAR(180)  │ NULL                    │   │
│  │ created_at        │ TIMESTAMP     │ NOT NULL                │   │
│  │ updated_at        │ TIMESTAMP     │ NOT NULL                │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ INDEXES:                                                    │   │
│  │ • idx_expenses_date      ON (expense_date)                 │   │
│  │ • idx_expenses_category  ON (category)                     │   │
│  │                                                              │   │
│  │ ESTIMATED SIZE: 1000-100K rows (depends on usage)          │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ├─────────────────────────────────────────────────────────────┐   │
│  │ TABLE: monthly_budgets                                      │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ COLUMN            │ TYPE          │ CONSTRAINTS             │   │
│  ├───────────────────┼───────────────┼─────────────────────────┤   │
│  │ id                │ BIGSERIAL     │ PRIMARY KEY             │   │
│  │ budget_year       │ INT           │ NOT NULL                │   │
│  │ budget_month      │ INT           │ NOT NULL                │   │
│  │ limit_amount      │ NUMERIC(12,2) │ NOT NULL                │   │
│  │ created_at        │ TIMESTAMP     │ NOT NULL                │   │
│  │ updated_at        │ TIMESTAMP     │ NOT NULL                │   │
│  ├─────────────────────────────────────────────────────────────┤   │
│  │ CONSTRAINTS:                                                │   │
│  │ • UNIQUE(budget_year, budget_month) [uk_budget_year_month] │   │
│  │                                                              │   │
│  │ ESTIMATED SIZE: 12-120 rows (max 12/year)                  │   │
│  └─────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  SCHEMA VERSION CONTROL:                                             │
│  └─ Flyway DB Migration: V1__init_schema.sql                        │
│     └─ Version 1: Initial schema with both tables                   │
│        └─ Auto-executed on application startup                      │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
```

---

## 3. Request-Response Flow Diagrams

### 3.1 Create Expense Flow
```
Client                    Controller              Service              Repository           Database
  │                          │                       │                      │                 │
  ├─ POST /expenses ────────>│                       │                      │                 │
  │   (CreateExpenseRequest) │                       │                      │                 │
  │                          │                       │                      │                 │
  │                          ├─ @Valid Validation   │                      │                 │
  │                          │  (spring-validation) │                      │                 │
  │                          │  ✓ amount > 0       │                      │                 │
  │                          │  ✓ description req  │                      │                 │
  │                          │                      │                      │                 │
  │                          ├─ call create() ────>│                      │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ validateAmount()   │                 │
  │                          │                      │  throw if invalid   │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ Expense.builder()  │                 │
  │                          │                      │  .description()     │                 │
  │                          │                      │  .amount()          │                 │
  │                          │                      │  .category()        │                 │
  │                          │                      │  .expenseDate()     │                 │
  │                          │                      │  .recurring()       │                 │
  │                          │                      │  .build()           │                 │
  │                          │                      │  (JPA Entity)       │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ expenseRepository  │                 │
  │                          │                      │  .save(expense) ───>│                 │
  │                          │                      │                      │                 │
  │                          │                      │                      ├─ @PrePersist   │
  │                          │                      │                      │  createdAt=now │
  │                          │                      │                      │  updatedAt=now │
  │                          │                      │                      │                 │
  │                          │                      │                      ├─ INSERT INTO   │
  │                          │                      │                      │  expenses ... ─┤─────>
  │                          │                      │                      │                 │
  │                          │                      │                      │                 │
  │                          │                      │                      │ <─ id (SERIAL)─┤
  │                          │                      │                      │                 │
  │                          │                      │ <─ Expense(id=X) ───│                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ toResponse()       │                 │
  │                          │                      │ (DTO converter)     │                 │
  │                          │                      │                      │                 │
  │                          │ <─ ExpenseResponse ──│                      │                 │
  │                          │  (with id, amounts)  │                      │                 │
  │                          │                      │                      │                 │
  │ <─ 200 OK ──────────────│                       │                      │                 │
  │ (JSON response)          │                      │                      │                 │
  │                          │                      │                      │                 │
```

### 3.2 Get Budget Status Flow
```
Client                    Controller              Service              Repository           Database
  │                          │                       │                      │                 │
  ├─ GET /budgets/2024/8 ──>│                       │                      │                 │
  │                          │                       │                      │                 │
  │                          ├─ @Min/@Max Validation│                      │                 │
  │                          │  (2000 ≤ year ≤2100)│                      │                 │
  │                          │  (1 ≤ month ≤ 12)   │                      │                 │
  │                          │                      │                      │                 │
  │                          ├─ call getStatus ───>│                       │                 │
  │                          │  (year, month)       │                      │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ budgetRepository    │                 │
  │                          │                      │  .findByYearAndMonth()            
  │                          │                      │  ────────────────────┤────────────>   │
  │                          │                      │                      │  SELECT * FROM │
  │                          │                      │                      │  monthly_budgets
  │                          │                      │                      │  WHERE year=2024
  │                          │                      │                      │  AND month=8  ─┤
  │                          │                      │                      │                 │
  │                          │                      │                      │ <─ MonthlyBudget
  │                          │                      │ <─ Optional<Budget> ─│  (limitAmount) │
  │                          │                      │                      │                 │
  │                          │                      ├─ expenseRepository   │                 │
  │                          │                      │  .findByExpenseDateBetween()
  │                          │                      │  (2024-08-01 to -31) │                 │
  │                          │                      │  ────────────────────┤────────────>   │
  │                          │                      │                      │  SELECT * FROM │
  │                          │                      │                      │  expenses       │
  │                          │                      │                      │  WHERE date IN  │
  │                          │                      │                      │  Aug-2024    ──┤
  │                          │                      │                      │                 │
  │                          │                      │                      │ <─ List<Expense>
  │                          │                      │ <─ List<Expense> ───│                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ calculateSpent()   │                 │
  │                          │                      │ (sum all amounts)    │                 │
  │                          │                      │ = $500.00           │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ getRemainingAmount │                 │
  │                          │                      │ = $1000.00 - $500.00│                 │
  │                          │                      │ = $500.00           │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ isOverBudget()     │                 │
  │                          │                      │ = false              │                 │
  │                          │                      │                      │                 │
  │                          │                      ├─ toResponse()       │                 │
  │                          │                      │ (BudgetStatusResponse)               │
  │                          │ <─ BudgetStatus ────│                      │                 │
  │                          │  {limitAmount:$1000 │                      │                 │
  │                          │   spent:$500        │                      │                 │
  │                          │   remaining:$500    │                      │                 │
  │                          │   isOverBudget:false}                       │                 │
  │                          │                      │                      │                 │
  │ <─ 200 OK ──────────────│                       │                      │                 │
  │                          │                      │                      │                 │
```

---

## 4. Component Interaction Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                  SPRING BOOT APPLICATION CONTEXT                     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Spring Framework Core Components                            │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │  • Dependency Injection Container (@Autowired)              │   │
│  │    └─ Manages bean lifecycle                                │   │
│  │                                                              │   │
│  │  • Transaction Manager (@Transactional)                     │   │
│  │    └─ ACID compliance for database operations               │   │
│  │                                                              │   │
│  │  • HTTP Server (Embedded Tomcat)                            │   │
│  │    └─ Listens on port 8080 (default)                        │   │
│  │                                                              │   │
│  │  • Error Handling                                            │   │
│  │    └─ @ControllerAdvice for global exception handling       │   │
│  │                                                              │   │
│  │  • Security (Spring Security)                               │   │
│  │    └─ Authentication & Authorization                        │   │
│  │    └─ CustomSecurityConfig                                  │   │
│  │                                                              │   │
│  │  • Validation (Spring Validation)                           │   │
│  │    └─ @Valid @NotNull @Min @Max annotations                 │   │
│  │                                                              │   │
│  │  • Data JPA (Spring Data JPA)                               │   │
│  │    └─ Hibernate ORM integration                             │   │
│  │    └─ Session factory & transaction boundaries              │   │
│  │                                                              │   │
│  │  • Actuator (Spring Boot Actuator)                          │   │
│  │    └─ Health checks, metrics, monitoring endpoints          │   │
│  │                                                              │   │
│  │  • API Documentation (SpringDoc OpenAPI)                    │   │
│  │    └─ Swagger UI on /swagger-ui.html                        │   │
│  │    └─ OpenAPI spec on /v3/api-docs                          │   │
│  │                                                              │   │
│  │  • Database Migration (Flyway)                              │   │
│  │    └─ Automatic schema version control                      │   │
│  │    └─ Runs migrations from db/migration/                    │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ Custom Application Beans                                    │   │
│  ├──────────────────────────────────────────────────────────────┤   │
│  │                                                              │   │
│  │  Repositories (Data Access):                                │   │
│  │  ├─ ExpenseRepository @Repository                           │   │
│  │  └─ MonthlyBudgetRepository @Repository                     │   │
│  │                                                              │   │
│  │  Services (Business Logic):                                 │   │
│  │  ├─ ExpenseService @Service                                 │   │
│  │  └─ BudgetService @Service                                  │   │
│  │                                                              │   │
│  │  Controllers (REST API):                                    │   │
│  │  ├─ ExpenseController @RestController                       │   │
│  │  └─ BudgetController @RestController                        │   │
│  │                                                              │   │
│  │  Configuration:                                              │   │
│  │  └─ SecurityConfig @Configuration                           │   │
│  │     (Spring Security bean definitions)                      │   │
│  │                                                              │   │
│  │  Exception Handlers:                                         │   │
│  │  └─ GlobalExceptionHandler @ControllerAdvice                │   │
│  │     ├─ handles NotFoundException                            │   │
│  │     ├─ handles BadRequestException                          │   │
│  │     ├─ handles ValidationException                          │   │
│  │     └─ returns standardized error responses                 │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                       │
└──────────────────────────────────────────────────────────────────────┘
       │                          │                          │
       ├──────────────────────────┼──────────────────────────┤
       ▼                          ▼                          ▼
  HTTP Requests          Database Operations        Cache Layer
  (Port 8080)            (PostgreSQL via JPA)        (Optional)
```

---

## 5. Data Flow & Sequence Diagram

```
Database Request Sequence: Create Expense

Entry Point: HTTP POST /api/v1/expenses
│
├─► 1. Spring MVC Dispatcher Servlet intercepts request
│   └─► Routes to ExpenseController
│
├─► 2. ExpenseController.create()
│   ├─ Validates request body with @Valid
│   ├─ Checks for @NotNull, @Min, @Max constraints
│   └─ Calls expenseService.create(request)
│
├─► 3. ExpenseService.create()
│   ├─ Calls validateAmount(request.amount())
│   │  └─ Throws BadRequestException if invalid
│   │
│   ├─ Builds Expense entity
│   │  ├─ expense.description = request.description().trim()
│   │  ├─ expense.amount = request.amount()
│   │  ├─ expense.category = request.category()
│   │  ├─ expense.expenseDate = request.expenseDate() ?? LocalDate.now()
│   │  ├─ expense.recurring = request.recurring()
│   │  └─ expense.note = request.note()
│   │
│   └─ Calls expenseRepository.save(expense)
│
├─► 4. ExpenseRepository.save() [JPA/Hibernate]
│   ├─ Detects entity is new (no ID)
│   ├─ Triggers @PrePersist lifecycle callback
│   │  ├─ Sets createdAt = LocalDateTime.now()
│   │  └─ Sets updatedAt = LocalDateTime.now()
│   │
│   └─ Executes SQL INSERT
│      └─ INSERT INTO expenses(description, amount, category, ...)
│         VALUES(?, ?, ?, ...)
│         RETURNING id;
│
├─► 5. Database executes INSERT
│   ├─ Validates NOT NULL constraints
│   ├─ Validates CHECK constraints (if any)
│   ├─ Inserts row with SERIAL id generation
│   └─ Returns generated ID to application
│
├─► 6. JPA populates returned ID into entity object
│   └─ expense.id = 123 (database-generated)
│
├─► 7. ExpenseService.toResponse()
│   └─ Converts Entity → DTO
│      return ExpenseResponse(id=123, description=..., amount=..., ...)
│
├─► 8. ExpenseController returns ResponseEntity
│   └─ return ResponseEntity.ok(expenseResponse)
│
└─► 9. Spring converts to JSON and sends HTTP 200 response
    └─ {"id": 123, "description": "...", "amount": "...", ...}
```

---

## 6. Technology Stack Diagram

```
┌──────────────────────────────────────────────────────────────────┐
│                   TECHNOLOGY STACK                               │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Language & Runtime:                                            │
│  ├─ Java 17                                                     │
│  ├─ Spring Boot 3.5.0                                           │
│  └─ Apache Maven 3.x (Build Tool)                               │
│                                                                  │
│  ──────────────────────────────────────────────────────────────│
│                                                                  │
│  SPRING ECOSYSTEM:                                              │
│  │                                                              │
│  ├─ Spring Framework Core                                       │
│  │  └─ Dependency Injection, IoC Container                     │
│  │                                                              │
│  ├─ Spring Boot                                                │
│  │  ├─ Auto-configuration                                      │
│  │  └─ Embedded Tomcat server                                  │
│  │                                                              │
│  ├─ Spring Data JPA                                            │
│  │  ├─ Repository abstraction                                  │
│  │  ├─ Hibernate ORM integration                               │
│  │  └─ Query DSL                                               │
│  │                                                              │
│  ├─ Spring Security                                            │
│  │  ├─ Authentication                                          │
│  │  ├─ Authorization                                           │
│  │  └─ CSRF protection                                         │
│  │                                                              │
│  ├─ Spring Validation                                          │
│  │  └─ Bean Validation (JSR-303/JSR-380)                       │
│  │                                                              │
│  ├─ Spring Actuator                                            │
│  │  ├─ Health endpoints                                        │
│  │  └─ Metrics collection                                      │
│  │                                                              │
│  └─ Spring Web (MVC)                                           │
│     ├─ Servlet handling                                        │
│     ├─ Request dispatching                                     │
│     └─ REST controller support                                 │
│                                                                  │
│  ──────────────────────────────────────────────────────────────│
│                                                                  │
│  DATABASE & ORM:                                                │
│  │                                                              │
│  ├─ PostgreSQL 14+                                             │
│  │  ├─ Primary data store                                      │
│  │  ├─ SERIAL ID generation                                    │
│  │  └─ ENUM types for categories                               │
│  │                                                              │
│  ├─ Hibernate 6.x                                              │
│  │  ├─ ORM framework                                           │
│  │  ├─ Session management                                      │
│  │  └─ Query translation                                       │
│  │                                                              │
│  ├─ Flyway                                                     │
│  │  └─ Database migration & versioning                         │
│  │                                                              │
│  ├─ HikariCP                                                   │
│  │  └─ Connection pooling                                      │
│  │                                                              │
│  └─ Jakarta Persistence API (JPA)                              │
│     ├─ Entity annotations                                      │
│     ├─ Lifecycle callbacks                                     │
│     └─ Query interface                                         │
│                                                                  │
│  ──────────────────────────────────────────────────────────────│
│                                                                  │
│  UTILITIES & LIBRARIES:                                         │
│  │                                                              │
│  ├─ Lombok                                                     │
│  │  ├─ @Getter @Setter                                         │
│  │  ├─ @NoArgsConstructor @AllArgsConstructor                  │
│  │  ├─ @Builder (Builder pattern)                              │
│  │  ├─ @RequiredArgsConstructor (Constructor injection)        │
│  │  └─ @Data, @ToString, @EqualsAndHashCode                    │
│  │                                                              │
│  ├─ SpringDoc OpenAPI 2.8.9                                    │
│  │  ├─ Swagger UI generation                                   │
│  │  ├─ OpenAPI 3.0 specification                               │
│  │  └─ Interactive API documentation                           │
│  │                                                              │
│  └─ Jakarta Validation                                         │
│     ├─ @Valid annotation                                       │
│     ├─ @NotNull @Min @Max                                      │
│     └─ Custom validators                                       │
│                                                                  │
│  ──────────────────────────────────────────────────────────────│
│                                                                  │
│  TESTING:                                                       │
│  │                                                              │
│  ├─ JUnit 5                                                    │
│  │  └─ Test execution framework                                │
│  │                                                              │
│  ├─ Mockito                                                    │
│  │  └─ Mock object framework                                   │
│  │                                                              │
│  ├─ Spring Boot Test                                           │
│  │  ├─ @SpringBootTest                                         │
│  │  ├─ @DataJpaTest                                            │
│  │  └─ Test slicing                                            │
│  │                                                              │
│  └─ AssertJ                                                    │
│     └─ Fluent assertion library                                │
│                                                                  │
│  ──────────────────────────────────────────────────────────────│
│                                                                  │
│  DEPLOYMENT & OPERATIONS:                                       │
│  │                                                              │
│  ├─ Docker                                                     │
│  │  ├─ Containerization                                        │
│  │  └─ Multi-container setup (docker-compose.yml)              │
│  │                                                              │
│  ├─ Docker Compose                                             │
│  │  ├─ Orchestrates application + database                     │
│  │  └─ Local dev environment                                   │
│  │                                                              │
│  └─ Maven                                                      │
│     ├─ Build automation                                        │
│     ├─ Dependency management                                   │
│     └─ Plugin ecosystem                                        │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 7. Error Handling Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│               ERROR HANDLING FLOW                                │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Request arrives at Controller                                  │
│         │                                                       │
│         ▼                                                       │
│  ┌──────────────────────────────────────────────┐              │
│  │ 1. Validation Layer (@Valid)                │              │
│  │    ├─ Checks annotations                    │              │
│  │    │  ├─ @NotNull                           │              │
│  │    │  ├─ @Min / @Max                        │              │
│  │    │  └─ @Valid (nested validation)         │              │
│  │    │                                        │              │
│  │    └─ On Error → MethodArgumentNotValidEx  │              │
│  │       └─ Caught by GlobalExceptionHandler  │              │
│  └────────┬─────────────────────────────────────┘              │
│           │ (if passes)                                        │
│           ▼                                                    │
│  ┌──────────────────────────────────────────────┐              │
│  │ 2. Business Logic Layer (Service)           │              │
│  │    ├─ validateAmount()                      │              │
│  │    │  └─ if (amount ≤ 0)                    │              │
│  │    │     throw BadRequestException          │              │
│  │    │                                        │              │
│  │    ├─ findById()                            │              │
│  │    │  └─ if (entity not found)              │              │
│  │    │     throw NotFoundException            │              │
│  │    │                                        │              │
│  │    └─ Other business validations           │              │
│  └────────┬─────────────────────────────────────┘              │
│           │ (if passes)                                        │
│           ▼                                                    │
│  ┌──────────────────────────────────────────────┐              │
│  │ 3. Data Access Layer (Repository)           │              │
│  │    ├─ Database operations                   │              │
│  │    ├─ Constraint violations                 │              │
│  │    │  └─ Unique constraint on (year,month) │              │
│  │    │  └─ NOT NULL violations                │              │
│  │    │                                        │              │
│  │    └─ DataIntegrityViolationException       │              │
│  └────────┬─────────────────────────────────────┘              │
│           │                                                    │
│           ▼                                                    │
│  ┌──────────────────────────────────────────────┐              │
│  │ 4. Exception Handler (@ControllerAdvice)    │              │
│  │    GlobalExceptionHandler                   │              │
│  │    ├─ @ExceptionHandler(NotFoundException)  │              │
│  │    │  └─ returns 404 with error message     │              │
│  │    │                                        │              │
│  │    ├─ @ExceptionHandler(BadRequestEx)      │              │
│  │    │  └─ returns 400 with error message     │              │
│  │    │                                        │              │
│  │    ├─ @ExceptionHandler(ValidationEx)      │              │
│  │    │  └─ returns 400 with field errors      │              │
│  │    │                                        │              │
│  │    └─ @ExceptionHandler(Exception)         │              │
│  │       └─ returns 500 with generic message   │              │
│  └────────┬─────────────────────────────────────┘              │
│           │                                                    │
│           ▼                                                    │
│  ┌──────────────────────────────────────────────┐              │
│  │ 5. Response to Client                       │              │
│  │                                             │              │
│  │  Successful:                                │              │
│  │  HTTP 200 OK + JSON response                │              │
│  │                                             │              │
│  │  Validation Error:                          │              │
│  │  HTTP 400 Bad Request                       │              │
│  │  {                                          │              │
│  │    "error": "Validation failed",           │              │
│  │    "fields": {...}                         │              │
│  │  }                                          │              │
│  │                                             │              │
│  │  Not Found:                                 │              │
│  │  HTTP 404 Not Found                         │              │
│  │  {                                          │              │
│  │    "error": "Expense not found: 999"       │              │
│  │  }                                          │              │
│  │                                             │              │
│  │  Server Error:                              │              │
│  │  HTTP 500 Internal Server Error             │              │
│  │  {                                          │              │
│  │    "error": "Internal server error"        │              │
│  │  }                                          │              │
│  └──────────────────────────────────────────────┘              │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 8. API Endpoints Map

```
┌──────────────────────────────────────────────────────────────────┐
│               REST API ENDPOINTS OVERVIEW                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  Base URL: http://localhost:8080/api/v1                         │
│                                                                  │
│  ┌─ EXPENSE ENDPOINTS ──────────────────────────────────────┐   │
│  │                                                           │   │
│  │  POST /expenses                                          │   │
│  │  ├─ Creates new expense                                 │   │
│  │  ├─ Body: CreateExpenseRequest (JSON)                   │   │
│  │  ├─ Response: 200 OK + ExpenseResponse                  │   │
│  │  └─ Example: /api/v1/expenses                           │   │
│  │                                                           │   │
│  │  PUT /expenses/{id}                                      │   │
│  │  ├─ Updates existing expense                            │   │
│  │  ├─ PathParam: id (Long)                                │   │
│  │  ├─ Body: UpdateExpenseRequest (JSON)                   │   │
│  │  ├─ Response: 200 OK + ExpenseResponse                  │   │
│  │  └─ Example: /api/v1/expenses/123                       │   │
│  │                                                           │   │
│  │  DELETE /expenses/{id}                                   │   │
│  │  ├─ Deletes expense record                              │   │
│  │  ├─ PathParam: id (Long)                                │   │
│  │  ├─ Response: 204 No Content                            │   │
│  │  └─ Example: /api/v1/expenses/123                       │   │
│  │                                                           │   │
│  │  GET /expenses                                           │   │
│  │  ├─ Lists all/filtered expenses                         │   │
│  │  ├─ Query Params:                                       │   │
│  │  │  ├─ month (optional): 1-12                           │   │
│  │  │  └─ category (optional): FOOD, TRANSPORT, etc        │   │
│  │  ├─ Response: 200 OK + List<ExpenseResponse>            │   │
│  │  └─ Examples:                                            │   │
│  │     /api/v1/expenses                                    │   │
│  │     /api/v1/expenses?month=8                            │   │
│  │     /api/v1/expenses?category=FOOD                      │   │
│  │     /api/v1/expenses?month=8&category=FOOD              │   │
│  │                                                           │   │
│  │  GET /expenses/{id}/export                              │   │
│  │  ├─ Exports expense data (CSV format)                   │   │
│  │  ├─ PathParam: id (Long)                                │   │
│  │  ├─ Response: 200 OK + File (CSV)                       │   │
│  │  └─ Example: /api/v1/expenses/123/export                │   │
│  │                                                           │   │
│  │  GET /expenses/stats                                     │   │
│  │  ├─ Gets expense statistics                             │   │
│  │  ├─ Query Params:                                       │   │
│  │  │  └─ month (optional): 1-12                           │   │
│  │  ├─ Response: 200 OK + StatisticsResponse               │   │
│  │  └─ Example: /api/v1/expenses/stats?month=8             │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ BUDGET ENDPOINTS ───────────────────────────────────────┐   │
│  │                                                           │   │
│  │  POST /budgets                                           │   │
│  │  ├─ Sets budget for a month                             │   │
│  │  ├─ Body: SetBudgetRequest (JSON)                       │   │
│  │  ├─ Response: 200 OK + BudgetStatusResponse             │   │
│  │  └─ Example: /api/v1/budgets                            │   │
│  │                                                           │   │
│  │  GET /budgets/{year}/{month}                             │   │
│  │  ├─ Gets budget status for specific month               │   │
│  │  ├─ PathParams:                                         │   │
│  │  │  ├─ year: 2000-2100                                  │   │
│  │  │  └─ month: 1-12                                      │   │
│  │  ├─ Response: 200 OK + BudgetStatusResponse             │   │
│  │  │  ├─ limitAmount                                      │   │
│  │  │  ├─ spentAmount                                      │   │
│  │  │  ├─ remainingAmount                                  │   │
│  │  │  └─ isOverBudget                                     │   │
│  │  └─ Example: /api/v1/budgets/2024/8                     │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ DOCUMENTATION & MONITORING ──────────────────────────────┐   │
│  │                                                           │   │
│  │  GET /swagger-ui.html                                    │   │
│  │  └─ Interactive API documentation                       │   │
│  │                                                           │   │
│  │  GET /v3/api-docs                                        │   │
│  │  └─ OpenAPI 3.0 specification (JSON)                    │   │
│  │                                                           │   │
│  │  GET /actuator                                           │   │
│  │  └─ Health check & metrics endpoints                    │   │
│  │                                                           │   │
│  │  GET /actuator/health                                    │   │
│  │  └─ Application health status                           │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 9. Deployment Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                  DEPLOYMENT ARCHITECTURE                         │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  LOCAL DEVELOPMENT (docker-compose.yml)                         │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ Docker Compose Orchestration                             │ │
│  │                                                            │ │
│  │  ┌──────────────────┐        ┌──────────────────┐        │ │
│  │  │  Container 1     │        │  Container 2     │        │ │
│  │  │  Application     │        │  PostgreSQL DB   │        │ │
│  │  │                  │        │                  │        │ │
│  │  │ Java 17          │        │ PostgreSQL 14    │        │ │
│  │  │ Spring Boot App  │        │ Database Engine  │        │ │
│  │  │ Port: 8080       │─────>  │ Port: 5432       │        │ │
│  │  │                  │        │ Volume: pgdata   │        │ │
│  │  │ Services:        │        │                  │        │ │
│  │  │ • ExpenseAPI     │        │ Tables:          │        │ │
│  │  │ • BudgetAPI      │        │ • expenses       │        │ │
│  │  │ • Swagger UI     │        │ • monthly_budgets│        │ │
│  │  │ • Actuator       │        │                  │        │ │
│  │  └──────────────────┘        └──────────────────┘        │ │
│  │           ▲                                              │ │
│  │           │ Shared Network (expense-tracker-network)    │ │
│  │           │                                              │ │
│  │     Persistent Volume                                   │ │
│  │     (pgdata) for database                               │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ AVAILABLE ACCESS POINTS                                    │ │
│  │                                                             │ │
│  │ Application:     http://localhost:8080                      │ │
│  │ Swagger UI:      http://localhost:8080/swagger-ui.html      │ │
│  │ Actuator:        http://localhost:8080/actuator             │ │
│  │ OpenAPI Spec:    http://localhost:8080/v3/api-docs          │ │
│  │                                                             │ │
│  │ Database:        localhost:5432                             │ │
│  │ Default DB:      expensetracker                             │ │
│  │ Default User:    postgres                                   │ │
│  │ Default Pass:    postgres                                   │ │
│  │                                                             │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                                                                  │
│  BUILD & DEPLOYMENT PIPELINE                                   │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │ 1. Source Code (Git Repository)                           │ │
│  │    └─ pom.xml, src/**, docker-compose.yml                │ │
│  │                                                            │ │
│  │ 2. Build Phase (Maven)                                   │ │
│  │    ├─ mvn clean compile                                  │ │
│  │    ├─ Run unit tests                                     │ │
│  │    ├─ mvn package                                        │ │
│  │    └─ Generate JAR: target/expense-tracker-0.0.1.jar     │ │
│  │                                                            │ │
│  │ 3. Docker Image Creation                                 │ │
│  │    ├─ Dockerfile (implied)                               │ │
│  │    ├─ Base: Java 17 image                                │ │
│  │    ├─ Copy JAR into image                                │ │
│  │    ├─ Expose port 8080                                   │ │
│  │    └─ ENTRYPOINT: java -jar expense-tracker.jar          │ │
│  │                                                            │ │
│  │ 4. Docker Compose                                        │ │
│  │    ├─ docker-compose build                               │ │
│  │    └─ docker-compose up -d                               │ │
│  │                                                            │ │
│  │ 5. Migration Execution                                   │ │
│  │    ├─ Flyway detects V1__init_schema.sql                 │ │
│  │    ├─ Executes schema creation                           │ │
│  │    └─ Marks migration as applied                         │ │
│  │                                                            │ │
│  │ 6. Application Ready                                     │ │
│  │    └─ Listening on http://localhost:8080                │ │
│  │                                                            │ │
│  └────────────────────────────────────────────────────────────┘ │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 10. Database Schema Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    DATABASE SCHEMA                              │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │                  expenses (Table)                        │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                           │  │
│  │  PK │ id              : BIGSERIAL                        │  │
│  │     │ description     : VARCHAR(160) NOT NULL            │  │
│  │     │ amount          : NUMERIC(12,2) NOT NULL           │  │
│  │     │ category        : VARCHAR(30) NOT NULL             │  │
│  │     │                  [FOOD|TRANSPORT|UTILITIES|        │  │
│  │     │                   ENTERTAINMENT|HEALTH|             │  │
│  │     │                   SHOPPING|OTHER]                  │  │
│  │     │ expense_date    : DATE NOT NULL                    │  │
│  │     │ recurring       : BOOLEAN NOT NULL DEFAULT FALSE   │  │
│  │     │ note            : VARCHAR(180)                     │  │
│  │     │ created_at      : TIMESTAMP NOT NULL               │  │
│  │     │ updated_at      : TIMESTAMP NOT NULL               │  │
│  │                                                           │  │
│  │  Indexes:                                                 │  │
│  │  ├─ idx_expenses_date (expense_date)                    │  │
│  │  └─ idx_expenses_category (category)                    │  │
│  │                                                           │  │
│  │  Constraints:                                             │  │
│  │  ├─ PRIMARY KEY (id)                                     │  │
│  │  └─ CHECK (amount > 0)  [Business logic]                │  │
│  │                                                           │  │
│  │  Typical Rows (Example):                                 │  │
│  │  ┌───┬─────────────────┬────────┬──────────┬──────────┐  │  │
│  │  │id │ description     │ amount │ category │   date   │  │  │
│  │  ├───┼─────────────────┼────────┼──────────┼──────────┤  │  │
│  │  │1  │ Groceries       │ 50.00  │ FOOD     │ 2024-08-01
│  │  │2  │ Gas             │ 45.00  │ TRANSPORT│ 2024-08-02
│  │  │3  │ Movie tickets   │ 25.00  │ ENTERT   │ 2024-08-05
│  │  │...│ ...             │ ...    │ ...      │ ...      │  │  │
│  │  └───┴─────────────────┴────────┴──────────┴──────────┘  │  │
│  │                                                           │  │
│  │  Relationships:                                            │  │
│  │  └─ No foreign keys (normalized for this domain)         │  │
│  │                                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│                         │                                       │
│                         │ No Direct Foreign Key                 │
│                         │ (Budget & Expenses are independent)   │
│                         │                                       │
│                         ▼                                       │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │            monthly_budgets (Table)                       │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │                                                           │  │
│  │  PK │ id              : BIGSERIAL                        │  │
│  │     │ budget_year     : INT NOT NULL                     │  │
│  │     │ budget_month    : INT NOT NULL (1-12)              │  │
│  │     │ limit_amount    : NUMERIC(12,2) NOT NULL           │  │
│  │     │ created_at      : TIMESTAMP NOT NULL               │  │
│  │     │ updated_at      : TIMESTAMP NOT NULL               │  │
│  │                                                           │  │
│  │  Indexes:                                                 │  │
│  │  └─ None (small table, rarely queried)                   │  │
│  │                                                           │  │
│  │  Constraints:                                             │  │
│  │  ├─ PRIMARY KEY (id)                                     │  │
│  │  ├─ UNIQUE(budget_year, budget_month)                    │  │
│  │  └─ CHECK (budget_month BETWEEN 1 AND 12)               │  │
│  │                                                           │  │
│  │  Typical Rows (Example):                                 │  │
│  │  ┌───┬─────────────┬──────┬──────────────┐              │  │
│  │  │id │ budget_year │ month│ limit_amount │              │  │
│  │  ├───┼─────────────┼──────┼──────────────┤              │  │
│  │  │1  │ 2024        │  1   │  1000.00     │              │  │
│  │  │2  │ 2024        │  2   │  1000.00     │              │  │
│  │  │3  │ 2024        │  8   │  1200.00     │              │  │
│  │  │...│ ...         │ ...  │  ...         │              │  │
│  │  └───┴─────────────┴──────┴──────────────┘              │  │
│  │                                                           │  │
│  │  Note: Budget amount is compared against                 │  │
│  │        SUM(expenses.amount) for same year/month         │  │
│  │        in BudgetService.calculateSpentAmount()          │  │
│  │                                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Flyway Version History (Implicit Table)          │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │  (Auto-created by Flyway)                                │  │
│  │                                                           │  │
│  │  Tracks applied migrations:                               │  │
│  │  ┌────────────┬────────────────────────────────────────┐ │  │
│  │  │ version    │ description                            │ │  │
│  │  ├────────────┼────────────────────────────────────────┤ │  │
│  │  │ 1          │ V1__init_schema.sql                   │ │  │
│  │  └────────────┴────────────────────────────────────────┘ │  │
│  │                                                           │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
│  SAMPLE QUERY OPERATIONS:                                      │
│  ├─ GET all expenses for August 2024:                         │  │
│  │  SELECT * FROM expenses                                   │  │
│  │  WHERE expense_date >= '2024-08-01'                       │  │
│  │  AND expense_date <= '2024-08-31'                         │  │
│  │                                                             │  │
│  ├─ GET budget for August 2024:                              │  │
│  │  SELECT * FROM monthly_budgets                            │  │
│  │  WHERE budget_year = 2024 AND budget_month = 8            │  │
│  │                                                             │  │
│  ├─ GET total spent in category for month:                   │  │
│  │  SELECT SUM(amount) FROM expenses                         │  │
│  │  WHERE category = 'FOOD'                                  │  │
│  │  AND expense_date >= '2024-08-01'                         │  │
│  │  AND expense_date <= '2024-08-31'                         │  │
│  │                                                             │  │
│  └─ GET spending by category:                                │  │
│     SELECT category, SUM(amount) as total                    │  │
│     FROM expenses                                            │  │
│     WHERE EXTRACT(YEAR FROM expense_date) = 2024             │  │
│     AND EXTRACT(MONTH FROM expense_date) = 8                 │  │
│     GROUP BY category                                        │  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 11. Security & Configuration Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│              SECURITY & CONFIGURATION LAYER                      │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ┌─ Spring Security Configuration ──────────────────────────┐   │
│  │ (SecurityConfig.java)                                   │   │
│  │                                                           │   │
│  │ @Configuration                                          │   │
│  │ public class SecurityConfig {                           │   │
│  │                                                           │   │
│  │   @Bean                                                 │   │
│  │   public SecurityFilterChain securityFilterChain()  {  │   │
│  │     // Configures security filters                     │   │
│  │     // - Authentication                                │   │
│  │     // - Authorization                                 │   │
│  │     // - CSRF protection                               │   │
│  │     // - HTTP security headers                         │   │
│  │   }                                                     │   │
│  │                                                           │   │
│  │   // Custom user authentication                         │   │
│  │   // JWT or session-based auth                          │   │
│  │                                                           │   │
│  │ }                                                        │   │
│  │                                                           │   │
│  │ Features:                                                │   │
│  │ ✓ Excluded UserDetailsServiceAutoConfiguration        │   │
│  │   (@SpringBootApplication exclude setting)             │   │
│  │ ✓ Custom authentication configuration                 │   │
│  │ ✓ CSRF protection enabled by default                  │   │
│  │ ✓ HTTP security headers configured                    │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ Application Configuration ──────────────────────────────┐   │
│  │ (application.properties)                                │   │
│  │                                                           │   │
│  │ # Server Configuration                                 │   │
│  │ server.port=8080                                       │   │
│  │ server.servlet.context-path=/                          │   │
│  │                                                           │   │
│  │ # Database Configuration                               │   │
│  │ spring.datasource.url=jdbc:postgresql://localhost:5432 │   │
│  │ spring.datasource.username=postgres                    │   │
│  │ spring.datasource.password=postgres                    │   │
│  │ spring.datasource.driver-class-name=...               │   │
│  │                                                           │   │
│  │ # JPA/Hibernate Configuration                          │   │
│  │ spring.jpa.database-platform=PostgreSQL...             │   │
│  │ spring.jpa.hibernate.ddl-auto=validate                 │   │
│  │ spring.jpa.show-sql=true (dev only)                    │   │
│  │                                                           │   │
│  │ # Flyway Configuration                                 │   │
│  │ spring.flyway.locations=classpath:db/migration         │   │
│  │ spring.flyway.enabled=true                             │   │
│  │                                                           │   │
│  │ # Logging Configuration                                │   │
│  │ logging.level.root=INFO                                │   │
│  │ logging.level.com.revati.expensetracker=DEBUG          │   │
│  │                                                           │   │
│  │ # OpenAPI/Swagger Configuration                        │   │
│  │ springdoc.api-docs.path=/v3/api-docs                   │   │
│  │ springdoc.swagger-ui.path=/swagger-ui.html             │   │
│  │                                                           │   │
│  │ # Actuator Configuration                               │   │
│  │ management.endpoints.web.exposure.include=health,info  │   │
│  │ management.endpoint.health.show-details=when-authorized │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ Environment-Specific Profiles ──────────────────────────┐   │
│  │                                                           │   │
│  │ application-dev.properties                              │   │
│  │ ├─ spring.jpa.show-sql=true                             │   │
│  │ ├─ logging.level.com.revati=DEBUG                       │   │
│  │ └─ Verbose output for development                       │   │
│  │                                                           │   │
│  │ application-test.properties                             │   │
│  │ ├─ spring.jpa.hibernate.ddl-auto=create-drop            │   │
│  │ ├─ H2 or in-memory database                             │   │
│  │ └─ Isolated test environment                            │   │
│  │                                                           │   │
│  │ Usage: java -Dspring.profiles.active=dev app.jar        │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ Dependency Injection & Beans ───────────────────────────┐   │
│  │                                                           │   │
│  │ Spring IoC Container manages:                            │   │
│  │                                                           │   │
│  │ Repositories (@Repository)                              │   │
│  │ ├─ ExpenseRepository                                    │   │
│  │ └─ MonthlyBudgetRepository                              │   │
│  │                                                           │   │
│  │ Services (@Service)                                     │   │
│  │ ├─ ExpenseService (@RequiredArgsConstructor)            │   │
│  │ └─ BudgetService (@RequiredArgsConstructor)             │   │
│  │                                                           │   │
│  │ Controllers (@RestController)                           │   │
│  │ ├─ ExpenseController (@RequiredArgsConstructor)         │   │
│  │ └─ BudgetController (@RequiredArgsConstructor)          │   │
│  │                                                           │   │
│  │ Configuration Beans (@Configuration)                    │   │
│  │ └─ SecurityConfig                                       │   │
│  │                                                           │   │
│  │ Features:                                                │   │
│  │ ✓ Constructor injection (via @RequiredArgsConstructor)  │   │
│  │ ✓ Singleton scope by default                            │   │
│  │ ✓ Circular dependency detection                         │   │
│  │ ✓ Lazy initialization                                   │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
│  ┌─ Transaction Management ─────────────────────────────────┐   │
│  │                                                           │   │
│  │ @Transactional Annotation Usage:                         │   │
│  │                                                           │   │
│  │ ExpenseService:                                          │   │
│  │ ├─ @Transactional                                        │   │
│  │ │  public ExpenseResponse create(request) {...}         │   │
│  │ │                                                         │   │
│  │ ├─ @Transactional                                        │   │
│  │ │  public ExpenseResponse update(id, request) {...}     │   │
│  │ │                                                         │   │
│  │ └─ @Transactional                                        │   │
│  │    public void delete(id) {...}                         │   │
│  │                                                           │   │
│  │ Features:                                                │   │
│  │ ✓ ACID compliance                                        │   │
│  │ ✓ Automatic rollback on exception                        │   │
│  │ ✓ Lazy loading within transaction boundary              │   │
│  │ ✓ Connection pooling (HikariCP)                          │   │
│  │                                                           │   │
│  └──────────────────────────────────────────────────────────┘   │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## 12. Complete System Summary

```
┌──────────────────────────────────────────────────────────────────┐
│            EXPENSE TRACKER - COMPLETE ARCHITECTURE SUMMARY        │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│  PROJECT: Expense Tracker API                                   │
│  VERSION: 0.0.1-SNAPSHOT                                        │
│  JAVA VERSION: 17                                               │
│  SPRING BOOT: 3.5.0                                             │
│  BUILD TOOL: Maven 3                                            │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  LAYERS (Top to Bottom):                                        │
│                                                                  │
│  1. PRESENTATION LAYER (API Controllers)                        │
│     • ExpenseController - CRUD operations for expenses         │
│     • BudgetController - Budget management endpoints           │
│     • Global error handling with @ControllerAdvice             │
│     • Input validation with @Valid & JSR-303 annotations       │
│                                                                  │
│  2. BUSINESS LOGIC LAYER (Services)                             │
│     • ExpenseService - Core expense business logic             │
│       ├─ Create, Update, Delete, List expenses                 │
│       ├─ Category-based filtering & statistics                 │
│       └─ Transactional integrity                               │
│                                                                  │
│     • BudgetService - Budget management                        │
│       ├─ Set monthly budget limits                             │
│       ├─ Calculate spent amount vs budget                      │
│       └─ Determine budget status                               │
│                                                                  │
│  3. DATA ACCESS LAYER (Repositories)                            │
│     • ExpenseRepository (JpaRepository)                         │
│       ├─ Inherited CRUD methods                                │
│       ├─ Custom query methods (find by category, date)         │
│       └─ Query DSL & @Query support                            │
│                                                                  │
│     • MonthlyBudgetRepository (JpaRepository)                   │
│       ├─ Find budget by year/month                             │
│       └─ Unique constraint enforcement                         │
│                                                                  │
│  4. PERSISTENCE LAYER (Database)                                │
│     • PostgreSQL 14+ Database                                  │
│     • Two main tables:                                          │
│       ├─ expenses (∞ rows, indexed by date & category)         │
│       └─ monthly_budgets (12/year rows, unique constraint)     │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  KEY FEATURES:                                                  │
│                                                                  │
│  ✓ RESTful API with clear endpoint structure                   │
│  ✓ Comprehensive data validation                               │
│  ✓ Transactional data consistency                              │
│  ✓ Exception handling & error responses                        │
│  ✓ Database versioning with Flyway                             │
│  ✓ OpenAPI/Swagger documentation                               │
│  ✓ Spring Security integration                                 │
│  ✓ Health checks & monitoring (Actuator)                       │
│  ✓ Environment-specific configurations                         │
│  ✓ Docker & Docker Compose support                             │
│  ✓ Unit & integration test framework                           │
│  ✓ Lombok code generation (reduces boilerplate)                │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  TECHNOLOGY COMPONENTS:                                         │
│                                                                  │
│  Runtime: Java 17 JVM, Spring Boot 3.5.0                        │
│  Web: Spring Web MVC, Embedded Tomcat                           │
│  Data: Spring Data JPA, Hibernate ORM, PostgreSQL              │
│  Security: Spring Security (customizable)                      │
│  Validation: Jakarta Validation (JSR-380)                      │
│  Database Migration: Flyway                                    │
│  API Docs: SpringDoc OpenAPI (Swagger UI)                      │
│  Monitoring: Spring Boot Actuator                              │
│  Build: Maven, Docker, Docker Compose                          │
│  Code Generation: Lombok                                       │
│  Testing: JUnit 5, Mockito, Spring Boot Test                   │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  DATA MODELS:                                                   │
│                                                                  │
│  Expense Entity:                                                │
│  ├─ id (PK)                                                    │
│  ├─ description (160 chars)                                    │
│  ├─ amount (NUMERIC 12,2)                                      │
│  ├─ category (ENUM: FOOD, TRANSPORT, UTILITIES, etc)          │
│  ├─ expenseDate (DATE)                                         │
│  ├─ recurring (BOOLEAN)                                        │
│  ├─ note (180 chars)                                           │
│  ├─ createdAt (TIMESTAMP, immutable)                           │
│  └─ updatedAt (TIMESTAMP)                                      │
│                                                                  │
│  MonthlyBudget Entity:                                          │
│  ├─ id (PK)                                                    │
│  ├─ year (INT)                                                 │
│  ├─ month (INT, 1-12)                                          │
│  ├─ limitAmount (NUMERIC 12,2)                                 │
│  ├─ createdAt (TIMESTAMP, immutable)                           │
│  └─ updatedAt (TIMESTAMP)                                      │
│     └─ Unique Constraint: (year, month)                        │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  DEPLOYMENT:                                                    │
│                                                                  │
│  Local Development:                                             │
│  └─ docker-compose up -d                                       │
│     • Spins up application container + PostgreSQL              │
│     • Accessible at http://localhost:8080                      │
│                                                                  │
│  Build Process:                                                 │
│  ├─ Source → Maven Compile                                     │
│  ├─ → Unit Tests → Code Coverage                               │
│  ├─ → Package JAR (with Flyway migrations)                     │
│  ├─ → Docker Image                                             │
│  └─ → Docker Compose Orchestration                             │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  ACCESS POINTS:                                                 │
│                                                                  │
│  Application API:    http://localhost:8080/api/v1               │
│  Swagger UI:         http://localhost:8080/swagger-ui.html      │
│  API Docs JSON:      http://localhost:8080/v3/api-docs          │
│  Health Check:       http://localhost:8080/actuator/health      │
│  Database:           localhost:5432 (PostgreSQL)                │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  SCALABILITY CONSIDERATIONS:                                    │
│                                                                  │
│  • Indexed queries on expense_date & category for performance  │
│  • Connection pooling (HikariCP) for database efficiency       │
│  • Transactional boundaries prevent dirty reads                │
│  • Stateless API design enables horizontal scaling             │
│  • Docker containerization for cloud deployment                │
│  • Environment-specific configurations for prod/dev/test       │
│                                                                  │
│  ────────────────────────────────────────────────────────────── │
│                                                                  │
│  FUTURE ENHANCEMENTS:                                           │
│                                                                  │
│  • User authentication & authorization (JWT)                   │
│  • Pagination for large result sets                            │
│  • Caching layer (Redis)                                       │
│  • Recurring expense automation                                │
│  • Report generation & export                                  │
│  • Mobile client application                                   │
│  • Real-time notifications                                     │
│  • Analytics & insights dashboard                              │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

---

## Summary

I've created a **complete, comprehensive end-to-end architecture diagram** for your Expense Tracker project that includes:

1. **High-Level System Architecture** - Overall data flow
2. **Layered Architecture** - Detailed breakdown of each layer
3. **Request-Response Flows** - Visual walkthroughs of API calls
4. **Component Interactions** - Spring Boot bean management
5. **Data Flow & Sequences** - Database operation sequences
6. **Technology Stack** - All frameworks and libraries
7. **Error Handling** - Exception processing flow
8. **API Endpoints Map** - Complete REST API reference
9. **Deployment Architecture** - Docker & Docker Compose setup
10. **Database Schema** - Table structures and relationships
11. **Security & Configuration** - Spring Security & properties
12. **System Summary** - Complete overview with key features

This architecture document covers everything from the HTTP client down to the PostgreSQL database, showing how each component interacts with others.