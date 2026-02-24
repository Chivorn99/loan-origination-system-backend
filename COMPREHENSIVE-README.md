# Loan Origination System - Comprehensive Documentation

## Overview
The Loan Origination System is a Java Spring Boot application for managing pawn shop loan operations. It provides comprehensive functionality for customer management, collateral tracking, loan processing, repayment handling, and master data management.

## Table of Contents
1. [Database Schema & Table Entities](#database-schema--table-entities)
2. [Module Endpoints](#module-endpoints)
3. [Business Logic Flows](#business-logic-flows)
4. [Loan Calculation Logic](#loan-calculation-logic)
5. [System Architecture](#system-architecture)

---

## Database Schema & Table Entities

### Enums
```sql
CustomerStatus: ACTIVE, INACTIVE, DELETED
CollateralStatus: AVAILABLE, PAWNED, REDEEMED, FORFEITED, DELETED
LoanStatus: PENDING, ACTIVE, REDEEMED, DEFAULTED, FORFEITED, CANCELLED
LoanEvent: CREATED, ISSUED, PARTIAL_PAYMENT, FULL_PAYMENT, OVERDUE, DEFAULTED, FORFEITED, CANCELLED
```

### Master Tables

#### 1. m_branch (Branch Management)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR | NOT NULL | Branch name |
| address | VARCHAR | | Branch address |
| phone | VARCHAR | | Contact phone |
| status | VARCHAR | DEFAULT 'ACTIVE' | Branch status |

#### 2. m_currency (Currency Management)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR | UNIQUE, NOT NULL | Currency code (USD, KHR) |
| name | VARCHAR | | Currency name |
| symbol | VARCHAR | | Currency symbol ($, ៛) |
| decimal_place | INTEGER | DEFAULT 2 | Decimal places |
| status | VARCHAR | DEFAULT 'ACTIVE' | Currency status |

#### 3. m_role (User Roles)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR | UNIQUE, NOT NULL | Role code |
| name | VARCHAR | NOT NULL | Role name |
| description | VARCHAR | | Role description |

#### 4. m_payment_method (Payment Methods)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR | UNIQUE, NOT NULL | Payment method code |
| name | VARCHAR | | Payment method name |
| status | VARCHAR | DEFAULT 'ACTIVE' | Status |

#### 5. m_payment_type (Payment Types)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| code | VARCHAR | | Payment type code |
| name | VARCHAR | | Payment type name |

### People Tables

#### 6. m_customer (Customer Management)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| full_name | VARCHAR | NOT NULL | Customer full name |
| phone | VARCHAR | | Contact phone |
| id_number | VARCHAR | UNIQUE, NOT NULL | National ID number |
| address | VARCHAR | | Customer address |
| status | CustomerStatus | DEFAULT 'ACTIVE' | Customer status |
| created_at | DATETIME | DEFAULT NOW() | Creation timestamp |
| updated_at | DATETIME | | Last update timestamp |
| deleted_at | DATETIME | | Soft delete timestamp |

#### 7. m_user (System Users)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| username | VARCHAR | UNIQUE, NOT NULL | Username |
| password | VARCHAR | NOT NULL | Hashed password |
| role_id | BIGINT | FK → m_role.id | User role |
| branch_id | BIGINT | FK → m_branch.id | Assigned branch |
| status | VARCHAR | DEFAULT 'ACTIVE' | User status |
| created_at | DATETIME | DEFAULT NOW() | Creation timestamp |

### Loan Core Tables

#### 8. pawn_item (Collateral Items)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| customer_id | BIGINT | FK → m_customer.id, NOT NULL | Customer owner |
| item_type | VARCHAR | | Type of item (jewelry, electronics, etc.) |
| description | VARCHAR | | Item description |
| estimated_value | DECIMAL | | Appraised value |
| photo_url | VARCHAR | | Photo URL |
| status | CollateralStatus | DEFAULT 'AVAILABLE' | Item status |
| created_at | DATETIME | DEFAULT NOW() | Creation timestamp |
| updated_at | DATETIME | | Last update timestamp |
| deleted_at | DATETIME | | Soft delete timestamp |

#### 9. pawn_loan (Loan Contracts)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| loan_code | VARCHAR | UNIQUE, NOT NULL | Unique loan code |
| customer_id | BIGINT | FK → m_customer.id, NOT NULL | Borrower |
| pawn_item_id | BIGINT | FK → pawn_item.id, NOT NULL | Collateral item |
| currency_id | BIGINT | FK → m_currency.id, NOT NULL | Loan currency |
| branch_id | BIGINT | FK → m_branch.id, NOT NULL | Originating branch |
| loan_amount | DECIMAL | NOT NULL | Principal amount |
| interest_rate | DECIMAL | NOT NULL | Interest rate (%) |
| total_payable_amount | DECIMAL | NOT NULL | Principal + interest |
| loan_date | DATE | DEFAULT NOW() | Loan origination date |
| due_date | DATE | | Repayment due date |
| status | LoanStatus | DEFAULT 'PENDING' | Loan status |
| created_at | DATETIME | DEFAULT NOW() | Creation timestamp |
| updated_at | DATETIME | | Last update timestamp |
| redeemed_at | DATETIME | | Redemption timestamp |
| defaulted_at | DATETIME | | Default timestamp |
| overdue_at | DATETIME | | Overdue timestamp |

#### 10. pawn_repayment (Loan Repayments)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| pawn_loan_id | BIGINT | FK → pawn_loan.id | Associated loan |
| currency_id | BIGINT | FK → m_currency.id | Payment currency |
| payment_method_id | BIGINT | FK → m_payment_method.id | Payment method |
| payment_type_id | BIGINT | FK → m_payment_type.id | Payment type |
| payment_date | DATE | DEFAULT NOW() | Payment date |
| paid_amount | DECIMAL | | Total paid amount |
| principal_paid | DECIMAL | | Principal portion |
| interest_paid | DECIMAL | | Interest portion |
| penalty_paid | DECIMAL | | Penalty portion |
| remaining_principal | DECIMAL | | Remaining principal after payment |
| received_by | BIGINT | FK → m_user.id | User who received payment |
| created_at | DATETIME | DEFAULT NOW() | Creation timestamp |

#### 11. pawn_forfeit (Forfeited Loans)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| pawn_loan_id | BIGINT | FK → pawn_loan.id | Forfeited loan |
| forfeit_date | DATE | | Forfeiture date |
| note | VARCHAR | | Forfeiture notes |

#### 12. cfg_loan (Loan Configuration)
| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AUTO_INCREMENT | Unique identifier |
| branch_id | BIGINT | FK → m_branch.id | Branch-specific config |
| currency_id | BIGINT | FK → m_currency.id | Currency-specific config |
| min_loan_amount | DECIMAL | | Minimum loan amount |
| max_loan_amount | DECIMAL | | Maximum loan amount |
| interest_rate | DECIMAL | | Default interest rate |
| interest_type | VARCHAR | | Interest type (simple, compound) |
| interest_period | VARCHAR | | Interest period (monthly, annually) |
| penalty_rate | DECIMAL | | Penalty rate for overdue |
| penalty_grace_days | INTEGER | | Grace period before penalty |
| max_loan_duration | INTEGER | | Maximum loan duration (days) |
| auto_forfeit_days | INTEGER | | Days before auto-forfeit |
| status | VARCHAR | | Configuration status |
| effective_from | DATE | | Configuration start date |
| effective_to | DATE | | Configuration end date |
| created_at | DATETIME | | Creation timestamp |

---

## Module Endpoints

### Base URL: `http://localhost:8080/api`

### 1. Customer Management Module (`/api/customers`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/customers` | Create new customer | `CustomerRequest` | `Customer` |
| GET | `/api/customers/{id}` | Get customer by ID | - | `Customer` |
| GET | `/api/customers/by-id-number/{idNumber}` | Get customer by national ID | - | `Customer` |
| PUT | `/api/customers/{id}` | Update customer | `CustomerRequest` | `Customer` |
| PATCH | `/api/customers/{id}` | Partial update | `CustomerPatchRequest` | `Customer` |
| GET | `/api/customers` | Get all customers (paginated) | Query params | `Page<Customer>` |
| DELETE | `/api/customers/{id}` | Soft delete customer | - | Success message |
| GET | `/api/customers/{id}/has-active-loans` | Check active loans | - | `Boolean` |

### 2. Collateral (Pawn Item) Management Module (`/api/pawn-items`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-items` | Create new collateral | `PawnItemRequest` | `PawnItem` |
| GET | `/api/pawn-items/{id}` | Get collateral by ID | - | `PawnItem` |
| GET | `/api/pawn-items/{id}/active` | Get active collateral | - | `PawnItem` |
| PUT | `/api/pawn-items/{id}` | Update collateral | `PawnItemRequest` | `PawnItem` |
| GET | `/api/pawn-items` | Get all items (paginated) | Query params | `Page<PawnItem>` |
| GET | `/api/pawn-items/customer/{customerId}` | Get items by customer | - | `List<PawnItem>` |
| GET | `/api/pawn-items/customer/{customerId}/page` | Get items by customer (paginated) | Query params | `Page<PawnItem>` |
| GET | `/api/pawn-items/status/{status}` | Get items by status | Query params | `Page<PawnItem>` |
| DELETE | `/api/pawn-items/{id}` | Soft delete item | - | Success message |
| GET | `/api/pawn-items/{id}/available` | Check availability | - | `Boolean` |

### 3. Loan Management Module (`/api/pawn-loans`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-loans` | Create new loan | `PawnLoanRequest` | `PawnLoan` |
| GET | `/api/pawn-loans/{id}` | Get loan by ID | - | `PawnLoan` |
| GET | `/api/pawn-loans/code/{loanCode}` | Get loan by code | - | `PawnLoan` |
| GET | `/api/pawn-loans` | Get all loans (paginated) | Query params | `Page<PawnLoan>` |
| GET | `/api/pawn-loans/customer/{customerId}` | Get loans by customer | - | `List<PawnLoan>` |
| GET | `/api/pawn-loans/customer/{customerId}/page` | Get loans by customer (paginated) | Query params | `Page<PawnLoan>` |
| GET | `/api/pawn-loans/status/{status}` | Get loans by status | Query params | `Page<PawnLoan>` |
| POST | `/api/pawn-loans/{id}/redeem` | Redeem a loan | - | `PawnLoan` |
| POST | `/api/pawn-loans/{id}/default` | Mark as defaulted | - | `PawnLoan` |
| POST | `/api/pawn-loans/calculate-total` | Calculate total payable | Query params | `Double` |
| GET | `/api/pawn-loans/{id}/overdue` | Check if overdue | - | `Boolean` |

### 4. Repayment Management Module (`/api/pawn-repayments`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-repayments` | Create repayment | `PawnRepaymentRequest` | `PawnRepayment` |
| GET | `/api/pawn-repayments/{id}` | Get repayment by ID | - | `PawnRepayment` |
| GET | `/api/pawn-repayments/loan/{loanId}` | Get repayment history | - | `List<PawnRepayment>` |
| GET | `/api/pawn-repayments/loan/{loanId}/page` | Get history (paginated) | Query params | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/loan/{loanId}/schedule` | Calculate schedule | - | `RepaymentSchedule` |
| GET | `/api/pawn-repayments/loan/{loanId}/total-paid` | Get total paid amount | - | `Double` |
| GET | `/api/pawn-repayments/by-date` | Get by date range | Query params | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/daily-collection/{branchId}` | Daily collection report | Query params | `DailyCollectionReport` |
| GET | `/api/pawn-repayments/today/{branchId}` | Today's repayments | - | `List<PawnRepayment>` |
| GET | `/api/pawn-repayments/customer/{customerId}/months/{months}` | Customer repayment logs | Query params | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/customer/{customerId}/summary/{months}` | Customer summary | - | `CustomerRepaymentSummary` |
| GET | `/api/pawn-repayments/overdue` | Get overdue loans | - | `List<RepaymentSchedule>` |

### 5. Branch Management Module (`/api/branches`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/branches` | Create new branch | `BranchRequest` | `Branch` |
| GET | `/api/branches/{id}` | Get branch by ID | - | `Branch` |
| PUT | `/api/branches/{id}` | Update branch | `BranchRequest` | `Branch` |
| PATCH | `/api/branches/{id}` | Partial update | `BranchPatchRequest` | `Branch` |
| GET | `/api/branches` | Get all branches (paginated) | Query params | `Page<Branch>` |
| DELETE | `/api/branches/{id}` | Soft delete branch | - | Success message |
| GET | `/api/branches/exists/{name}` | Check if exists | - | `Boolean` |

### 6. Currency Management Module (`/api/currencies`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/currencies` | Create new currency | `CurrencyRequest` | `CurrencyResponse` |
| GET | `/api/currencies/{id}` | Get currency by ID | - | `CurrencyResponse` |
| PUT | `/api/currencies/{id}` | Update currency | `CurrencyRequest` | `CurrencyResponse` |
| PATCH | `/api/currencies/{id}` | Partial update | `CurrencyPatchRequest` | `CurrencyResponse` |
| GET | `/api/currencies` | Get all currencies (paginated) | Query params | `Page<CurrencyResponse>` |
| DELETE | `/api/currencies/{id}` | Soft delete currency | - | Success message |
| GET | `/api/currencies/exists/{code}` | Check if exists | - | `Boolean` |

### 7. Loan Configuration Module (`/api/cfg-loans`)
| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/cfg-loans` | Create loan config | `CfgLoanRequest` | `CfgLoan` |
| GET | `/api/cfg-loans/{id}` | Get config by ID | - | `CfgLoan` |
| PUT | `/api/cfg-loans/{id}` | Update config | `CfgLoanRequest` | `CfgLoan` |
| PATCH | `/api/cfg-loans/{id}` | Partial update | `CfgLoanPatchRequest` | `CfgLoan` |
| GET | `/api/cfg-loans` | Get all configs (paginated) | Query params | `Page<CfgLoan>` |
| GET | `/api/cfg-loans/active` | Get active configs | - | `List<CfgLoan>` |
| GET | `/api/cfg-loans/branch/{branchId}/currency/{currencyId}` | Get config by branch/currency | - | `CfgLoan` |

### 8. Master Data Modules (No Controllers Yet)
The following entities have repositories but no REST controllers implemented:

- **PaymentMethod**: `m_payment_method` table - Would need endpoints at `/api/payment-methods`
- **PaymentType**: `m_payment_type` table - Would need endpoints at `/api/payment-types`
- **Role**: `m_role` table - Would need endpoints at `/api/roles`
- **User**: `m_user` table - Would need endpoints at `/api/users`
- **PawnForfeit**: `pawn_forfeit` table - Would need endpoints at `/api/pawn-forfeits`

---

## Business Logic Flows

### 1. Loan Creation Flow
```
1. Customer Registration → Customer created in system
2. Collateral Registration → PawnItem created with estimated value
3. Loan Application → PawnLoan created with validation:
   - Collateral must be AVAILABLE
   - Loan amount ≤ 70% of collateral estimated value
   - Customer must be ACTIVE
   - Branch and currency must exist
4. Interest Calculation → Total payable amount calculated
5. Loan Issuance → Status changes from CREATED to ACTIVE
6. Collateral Status Update → PawnItem status changes to PAWNED
```

### 2. Loan Repayment Flow
```
1. Payment Recording → PawnRepayment created with:
   - Principal, interest, penalty breakdown
   - Payment method and type
   - Received by user
2. Balance Update → Remaining principal calculated
3. Status Transition:
   - If total paid ≥ total payable → Loan REDEEMED, collateral AVAILABLE
   - If partial payment → Loan remains ACTIVE
   - If overdue payment → Penalty applied
```

### 3. Loan Default & Forfeiture Flow
```
1. Overdue Detection → Scheduled job checks due dates daily
2. Grace Period → Configurable grace days before penalty
3. Default Status → Loan marked DEFAULTED after grace period
4. Forfeiture Process → Collateral marked FORFEITED
5. Forfeiture Record → PawnForfeit entry created
```

### 4. Collateral Lifecycle
```
AVAILABLE → PAWNED → (REDEEMED → AVAILABLE) or (FORFEITED)
```

### 5. Customer Status Management
```
ACTIVE → INACTIVE (manual) → DELETED (soft delete)
Customer cannot be deleted if they have active loans
```

---

## Loan Calculation Logic

### 1. Total Payable Amount Calculation
**Formula:** `Total Payable = Principal + (Principal × Interest Rate / 100)`

**Java Implementation:**
```java
public BigDecimal calculateTotalPayableAmount(BigDecimal principalAmount, BigDecimal interestRate) {
    BigDecimal interestAmount = principalAmount
        .multiply(interestRate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
    return principalAmount.add(interestAmount).setScale(2, RoundingMode.HALF_UP);
}
```

**Example:**
- Principal: $1,000
- Interest Rate: 5%
- Interest: $1,000 × 0.05 = $50
- Total Payable: $1,050

### 2. Loan Amount Validation
**Business Rule:** Loan amount cannot exceed 70% of collateral estimated value

**Formula:** `Maximum Loan = Estimated Value × 0.70`

**Java Implementation:**
```java
BigDecimal maxLoanAmount = pawnItem.getEstimatedValue()
    .multiply(new BigDecimal("0.70"))
    .setScale(2, RoundingMode.HALF_UP);
    
if (loan.getLoanAmount().compareTo(maxLoanAmount) > 0) {
    throw new BusinessException("LOAN_AMOUNT_EXCEEDS_LIMIT",
        String.format("Loan amount %.2f exceeds maximum allowed %.2f (70%% of collateral value %.2f)",
            loan.getLoanAmount(), maxLoanAmount, pawnItem.getEstimatedValue()));
}
```

### 3. Overdue Penalty Calculation
**Formula:** `Penalty = Remaining Balance × Penalty Rate × Months Overdue`

**Java Implementation:**
```java
BigDecimal monthlyPenaltyRate = new BigDecimal("0.01"); // 1% per month
BigDecimal monthsOverdue = new BigDecimal(daysOverdue).divide(new BigDecimal("30"), 2, RoundingMode.HALF_UP);
BigDecimal penaltyAmount = remainingBalance.multiply(monthlyPenaltyRate).multiply(monthsOverdue);
```

**Example:**
- Remaining Balance: $500
- Days Overdue: 45 days (1.5 months)
- Penalty Rate: 1% per month
- Penalty: $500 × 0.01 × 1.5 = $7.50

### 4. Repayment Allocation Logic
When a payment is made, amounts are allocated in this priority:
1. **Penalty** (if any overdue)
2. **Interest** (accrued interest)
3. **Principal** (remaining principal balance)

**Validation:** `Paid Amount = Principal Paid + Interest Paid + Penalty Paid`

### 5. Interest Calculation Methods
The system supports different interest calculation methods via `CfgLoan`:

- **Simple Interest:** `Interest = Principal × Rate × Time`
- **Compound Interest:** `Total = Principal × (1 + Rate)^Time`
- **Flat Interest:** Fixed interest amount regardless of time

### 6. Repayment Schedule Calculation
**Endpoint:** `GET /api/pawn-repayments/loan/{loanId}/schedule`

**Returns:**
- Total payable amount
- Total paid amount
- Remaining balance
- Due date
- Overdue status
- Days overdue (if applicable)
- Estimated penalty (if overdue)

---

## System Architecture

### Technology Stack
- **Backend:** Java 17, Spring Boot 3.x
- **Database:** MySQL with JPA/Hibernate
- **Build Tool:** Maven
- **API Documentation:** OpenAPI/Swagger
- **Testing:** JUnit 5, Mockito
- **Containerization:** Docker, Kubernetes

### Key Design Patterns
1. **Repository Pattern** - Data access abstraction
2. **Service Layer Pattern** - Business logic encapsulation
3. **DTO Pattern** - Data transfer objects for API
4. **State Machine Pattern** - Loan status transitions
5. **Strategy Pattern** - Different interest calculation methods

### State Machine Transitions (LoanStateMachine)
```
CREATED → ISSUED → ACTIVE
ACTIVE → PARTIAL_PAYMENT → ACTIVE
ACTIVE → FULL_PAYMENT → REDEEMED
ACTIVE → OVERDUE → DEFAULTED
DEFAULTED → FORFEITED
Any state → CANCELLED
```

### Scheduled Jobs
1. **Overdue Loan Checker** - Daily at midnight
2. **Auto-Forfeit Processor** - After grace period expires
3. **Interest Accrual Calculator** - Monthly interest calculation

### Security Features
- Role-based access control (RBAC)
- Branch-level data isolation
- Soft delete for all major entities
- Audit logging for financial transactions
- Input validation and business rule enforcement

---

## Data Validation Rules

### Customer Validation
- National ID must be unique
- Phone number format validation
- Cannot delete customer with active loans

### Collateral Validation
- Estimated value must be positive
- Item must be AVAILABLE for new loans
- Cannot delete pawned items

### Loan Validation
- Interest rate must be between 0.1% and 100%
- Due date must be after loan date
- Loan amount must be within branch/currency limits
- Customer must be ACTIVE

### Repayment Validation
- Payment amount must be positive
- Sum of principal, interest, penalty must equal paid amount
- Cannot make payment on REDEEMED or FORFEITED loans
- Payment date cannot be in the future

---

## Error Handling

### Business Exception Codes
- `CUSTOMER_NOT_FOUND` - Customer does not exist
- `COLLATERAL_NOT_AVAILABLE` - Item not available for pawn
- `LOAN_AMOUNT_EXCEEDS_LIMIT` - Exceeds 70% of collateral value
- `LOAN_NOT_ACTIVE` - Cannot redeem inactive loan
- `PAYMENT_EXCEEDS_TOTAL` - Payment exceeds total payable
- `DUPLICATE_CONFIGURATION` - Active loan config already exists

### Global Exception Handler
- Returns structured `ApiResponse` with error code and message
- HTTP status codes: 400 (Bad Request), 404 (Not Found), 409 (Conflict)
- Validation errors include field-level details

---

## Testing Strategy

### Unit Tests
- Service layer business logic
- Repository queries
- DTO validation
- State machine transitions

### Integration Tests
- API endpoint testing
- Database operations
- Transaction management
- Scheduled job execution

### Postman Collections
- Complete endpoint testing
- Environment variables for different branches
- Test data setup and cleanup

---

## Deployment & Operations

### Environment Configuration
- **Development:** Local MySQL, port 8080
- **Testing:** Test database with mock data
- **Production:** Cloud MySQL, load balancing

### Monitoring & Logging
- Application metrics via Spring Boot Actuator
- Transaction logging for audit trail
- Error tracking and alerting
- Performance monitoring

### Backup & Recovery
- Daily database backups
- Transaction log archiving
- Disaster recovery procedures
