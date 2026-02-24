# Loan Origination System - Complete Endpoint Summary for Postman Testing

## Overview
This document provides a comprehensive summary of all REST API endpoints in the Loan Origination System, organized by module and entity. Each endpoint includes HTTP method, URL, description, and request/response structures for Postman testing.

## Base URL
All endpoints are relative to: `http://localhost:8080/api`

## 1. Customer Management Module

### Entity: Customer
**Base URL:** `/api/customers`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/customers` | Create new customer | `CustomerRequest` | `Customer` |
| GET | `/api/customers/{id}` | Get customer by ID | - | `Customer` |
| GET | `/api/customers/by-id-number/{idNumber}` | Get customer by national ID | - | `Customer` |
| PUT | `/api/customers/{id}` | Update customer | `CustomerRequest` | `Customer` |
| PATCH | `/api/customers/{id}` | Partial update customer | `CustomerPatchRequest` | `Customer` |
| GET | `/api/customers` | Get all customers (paginated) | Query params: `page`, `size`, `sortBy`, `direction`, `status` | `Page<Customer>` |
| DELETE | `/api/customers/{id}` | Soft delete customer | - | Success message |
| GET | `/api/customers/{id}/has-active-loans` | Check if customer has active loans | - | `Boolean` |

#### Data Structures:
- **CustomerRequest**: `{fullName: string, phone: string, idNumber: string, address: string}`
- **CustomerPatchRequest**: `{fullName: string, phone: string, idNumber: string, address: string}`
- **Customer**: `{id: long, fullName: string, phone: string, idNumber: string, address: string, status: CustomerStatus, createdAt: LocalDateTime, updatedAt: LocalDateTime, deletedAt: LocalDateTime}`
- **CustomerStatus**: `ACTIVE`, `INACTIVE`, `DELETED`

## 2. Collateral (Pawn Item) Management Module

### Entity: PawnItem
**Base URL:** `/api/pawn-items`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-items` | Create new collateral item | `PawnItemRequest` | `PawnItem` |
| GET | `/api/pawn-items/{id}` | Get collateral by ID | - | `PawnItem` |
| GET | `/api/pawn-items/{id}/active` | Get active collateral (excluding deleted) | - | `PawnItem` |
| PUT | `/api/pawn-items/{id}` | Update collateral item | `PawnItemRequest` | `PawnItem` |
| GET | `/api/pawn-items` | Get all collateral items (paginated) | Query params: `page`, `size`, `sortBy`, `direction` | `Page<PawnItem>` |
| GET | `/api/pawn-items/customer/{customerId}` | Get collateral items by customer ID | - | `List<PawnItem>` |
| GET | `/api/pawn-items/customer/{customerId}/page` | Get collateral items by customer ID (paginated) | Query params: `page`, `size` | `Page<PawnItem>` |
| GET | `/api/pawn-items/status/{status}` | Get collateral items by status | Query params: `page`, `size` | `Page<PawnItem>` |
| DELETE | `/api/pawn-items/{id}` | Soft delete collateral item | - | Success message |
| GET | `/api/pawn-items/{id}/available` | Check if collateral is available for pawn | - | `Boolean` |

#### Data Structures:
- **PawnItemRequest**: `{customerId: long, itemType: string, description: string, estimatedValue: BigDecimal, photoUrl: string}`
- **PawnItem**: Includes customer relationship, status, timestamps
- **CollateralStatus**: `AVAILABLE`, `PAWNED`, `REDEEMED`, `FORFEITED`, `DELETED`

## 3. Loan Management Module

### Entity: PawnLoan
**Base URL:** `/api/pawn-loans`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-loans` | Create new loan | `PawnLoanRequest` | `PawnLoan` |
| GET | `/api/pawn-loans/{id}` | Get loan by ID | - | `PawnLoan` |
| GET | `/api/pawn-loans/code/{loanCode}` | Get loan by loan code | - | `PawnLoan` |
| GET | `/api/pawn-loans` | Get all loans (paginated) | Query params: `page`, `size`, `sortBy`, `direction` | `Page<PawnLoan>` |
| GET | `/api/pawn-loans/customer/{customerId}` | Get loans by customer ID | - | `List<PawnLoan>` |
| GET | `/api/pawn-loans/customer/{customerId}/page` | Get loans by customer ID (paginated) | Query params: `page`, `size` | `Page<PawnLoan>` |
| GET | `/api/pawn-loans/status/{status}` | Get loans by status | Query params: `page`, `size` | `Page<PawnLoan>` |
| POST | `/api/pawn-loans/{id}/redeem` | Redeem a loan | - | `PawnLoan` |
| POST | `/api/pawn-loans/{id}/default` | Mark loan as defaulted | - | `PawnLoan` |
| POST | `/api/pawn-loans/calculate-total` | Calculate total payable amount | Query params: `principalAmount`, `interestRate` | `Double` |
| GET | `/api/pawn-loans/{id}/overdue` | Check if loan is overdue | - | `Boolean` |

#### Data Structures:
- **PawnLoanRequest**: `{customerId: long, pawnItemId: long, currencyId: long, branchId: long, loanAmount: BigDecimal, interestRate: BigDecimal, dueDate: LocalDate}`
- **PawnLoan**: Includes relationships with Customer, PawnItem, Currency, Branch
- **LoanStatus**: `ACTIVE`, `REDEEMED`, `DEFAULTED`, `FORFEITED`

## 4. Repayment Management Module

### Entity: PawnRepayment
**Base URL:** `/api/pawn-repayments`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-repayments` | Create new repayment | `PawnRepaymentRequest` | `PawnRepayment` |
| GET | `/api/pawn-repayments/{id}` | Get repayment by ID | - | `PawnRepayment` |
| GET | `/api/pawn-repayments/loan/{loanId}` | Get repayment history for a loan | - | `List<PawnRepayment>` |
| GET | `/api/pawn-repayments/loan/{loanId}/page` | Get repayment history (paginated) | Query params: `page`, `size`, `sortBy`, `direction` | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/loan/{loanId}/schedule` | Calculate repayment schedule | - | `RepaymentSchedule` |
| GET | `/api/pawn-repayments/loan/{loanId}/total-paid` | Get total paid amount for a loan | - | `Double` |
| GET | `/api/pawn-repayments/by-date` | Get repayments by date range | Query params: `startDate`, `endDate`, `page`, `size` | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/daily-collection/{branchId}` | Get daily collection report | Query params: `date` (optional) | `DailyCollectionReport` |
| GET | `/api/pawn-repayments/today/{branchId}` | Get today's repayments for a branch | - | `List<PawnRepayment>` |
| GET | `/api/pawn-repayments/customer/{customerId}/months/{months}` | Get customer repayment logs for N months | Query params: `page`, `size`, `sortBy`, `direction` | `Page<PawnRepayment>` |
| GET | `/api/pawn-repayments/customer/{customerId}/summary/{months}` | Get customer repayment summary for N months | - | `CustomerRepaymentSummary` |
| GET | `/api/pawn-repayments/overdue` | Get overdue loans with repayment status | - | `List<RepaymentSchedule>` |

#### Data Structures:
- **PawnRepaymentRequest**: `{pawnLoanId: long, currencyId: long, paymentMethodId: long, paymentTypeId: long, paidAmount: BigDecimal, principalPaid: BigDecimal, interestPaid: BigDecimal, penaltyPaid: BigDecimal, remainingPrincipal: BigDecimal, receivedBy: long, paymentDate: LocalDate}`
- **PawnRepayment**: Includes relationships with PawnLoan, Currency, PaymentMethod, PaymentType, User
- **Service Objects**: `RepaymentSchedule`, `DailyCollectionReport`, `CustomerRepaymentSummary`

## 5. Branch Management Module

### Entity: Branch
**Base URL:** `/api/branches`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/branches` | Create new branch | `BranchRequest` | `Branch` |
| GET | `/api/branches/{id}` | Get branch by ID | - | `Branch` |
| PUT | `/api/branches/{id}` | Update branch | `BranchRequest` | `Branch` |
| PATCH | `/api/branches/{id}` | Partial update branch | `BranchPatchRequest` | `Branch` |
| GET | `/api/branches` | Get all branches (paginated) | Query params: `page`, `size`, `sortBy`, `direction`, `status` | `Page<Branch>` |
| DELETE | `/api/branches/{id}` | Soft delete branch (set status to INACTIVE) | - | Success message |
| GET | `/api/branches/exists/{name}` | Check if branch exists by name | - | `Boolean` |

#### Data Structures:
- **BranchRequest**: `{name: string, address: string, phone: string, status: string}`
- **BranchPatchRequest**: `{name: string, address: string, phone: string, status: string}`
- **Branch**: `{id: long, name: string, address: string, phone: string, status: string}`

## 6. Currency Management Module

### Entity: Currency
**Base URL:** `/api/currencies`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/currencies` | Create new currency | `CurrencyRequest` | `CurrencyResponse` |
| GET | `/api/currencies/{id}` | Get currency by ID | - | `CurrencyResponse` |
| PUT | `/api/currencies/{id}` | Update currency | `CurrencyRequest` | `CurrencyResponse` |
| PATCH | `/api/currencies/{id}` | Partial update currency | `CurrencyPatchRequest` | `CurrencyResponse` |
| GET | `/api/currencies` | Get all currencies (paginated) | Query params: `page`, `size`, `sortBy`, `direction`, `status` | `Page<CurrencyResponse>` |
| DELETE | `/api/currencies/{id}` | Soft delete currency (set status to INACTIVE) | - | Success message |
| GET | `/api/currencies/exists/{code}` | Check if currency exists by code | - | `Boolean` |

#### Data Structures:
- **CurrencyRequest**: `{code: string, name: string, symbol: string, decimalPlace: integer, status: string}`
- **CurrencyPatchRequest**: `{code: string, name: string, symbol: string, decimalPlace: integer, status: string}`
- **CurrencyResponse**: `{id: long, code: string, name: string, symbol: string, decimalPlace: integer, status: string}`

## 7. Master Data Modules (Entities Without Controllers)

Based on the repository files, the system includes these master entities that have repositories but no controllers implemented yet:

### Entity: PaymentMethod
**Table:** `m_payment_method`
**Repository:** `PaymentMethodRepository`
**Entity Structure:**
```java
{
  id: Long,
  code: String (unique, not null),
  name: String,
  status: String (default: "ACTIVE")
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/payment-methods`

### Entity: PaymentType
**Table:** `m_payment_type`
**Repository:** `PaymentTypeRepository`
**Entity Structure:**
```java
{
  id: Long,
  code: String,
  name: String
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/payment-types`

### Entity: Role
**Table:** `m_role`
**Repository:** `RoleRepository` (not found, but entity exists)
**Entity Structure:**
```java
{
  id: Long,
  code: String (unique, not null),
  name: String (not null),
  description: String
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/roles`

### Entity: User
**Table:** `m_user`
**Repository:** `UserRepository`
**Entity Structure:**
```java
{
  id: Long,
  username: String (unique, not null),
  email: String (unique, not null),
  password: String (not null),
  role: Role (relationship),
  branch: Branch (relationship),
  status: String (default: "ACTIVE"),
  createdAt: LocalDateTime
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/users`

### Entity: CfgLoan (Loan Configuration)
**Table:** `cfg_loan`
**Repository:** No repository found
**Entity Structure:**
```java
{
  id: Long,
  branch: Branch (relationship),
  currency: Currency (relationship),
  minLoanAmount: BigDecimal,
  maxLoanAmount: BigDecimal,
  interestRate: BigDecimal,
  interestType: String,
  interestPeriod: String,
  penaltyRate: BigDecimal,
  penaltyGraceDays: Integer,
  maxLoanDuration: Integer,
  autoForfeitDays: Integer,
  status: String,
  effectiveFrom: LocalDate,
  effectiveTo: LocalDate,
  createdAt: LocalDateTime
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/loan-configurations`

### Entity: PawnForfeit
**Table:** `pawn_forfeit`
**Repository:** No repository found
**Entity Structure:**
```java
{
  id: Long,
  pawnLoan: PawnLoan (one-to-one relationship),
  forfeitDate: LocalDate,
  note: String
}
```
**Note:** No REST endpoints implemented. Would need CRUD endpoints at `/api/pawn-forfeits`

## 8. Mock Data for Postman Testing

### Customer Data Example:
```json
{
  "fullName": "John Doe",
  "phone": "+85512345678",
  "idNumber": "123456789012",
  "address": "Phnom Penh, Cambodia"
}
```

### Pawn Item Data Example:
```json
{
  "customerId": 1,
  "itemType": "Gold Necklace",
  "description": "24K gold necklace with pendant",
  "estimatedValue": 1500.00,
  "photoUrl": "https://example.com/photo.jpg"
}
```

### Loan Data Example:
```json
{
  "customerId": 1,
  "pawnItemId": 1,
  "currencyId": 1,
  "branchId": 1,
  "loanAmount": 1000.00,
  "interestRate": 5.0,
  "dueDate": "2024-12-31"
}
```

### Repayment Data Example:
```json
{
  "pawnLoanId": 1,
  "currencyId": 1,
  "paymentMethodId": 1,
  "paymentTypeId": 1,
  "paidAmount": 1050.00,
  "principalPaid": 1000.00,
  "interestPaid": 50.00,
  "penaltyPaid": 0.00,
  "remainingPrincipal": 0.00,
  "receivedBy": 1,
  "paymentDate": "2024-06-15"
}
```

### Branch Data Example:
```json
{
  "name": "Phnom Penh Main Branch",
  "address": "Street 123, Phnom Penh, Cambodia",
  "phone": "+855123456789",
  "status": "ACTIVE"
}
```

### Currency Data Example:
```json
{
  "code": "USD",
  "name": "US Dollar",
  "symbol": "$",
  "decimalPlace": 2,
  "status": "ACTIVE"
}
```

## 9. Testing Sequence for Postman

### Recommended Testing Order:
1. **Setup Master Data:**
   - Create branches (`POST /api/branches`)
   - Create currencies (`POST /api/currencies`)
   - (If implemented) Create payment methods, payment types, roles, users

2. **Customer Flow:**
   - Create customer (`POST /api/customers`)
   - Get customer by ID (`GET /api/customers/{id}`)
   - Update customer (`PUT /api/customers/{id}`)
   - List customers (`GET /api/customers`)

   - List pawn items by customer (`GET /api/pawn-items/customer/{customerId}`)
   - Update pawn item (`PUT /api/pawn-items/{id}`)

4. **Loan Flow:**
   - Create loan (`POST /api/pawn-loans`)
   - Get loan by ID (`GET /api/pawn-loans/{id}`)
   - Get loan by code (`GET /api/pawn-loans/code/{loanCode}`)
   - List loans by customer (`GET /api/pawn-loans/customer/{customerId}`)
   - Check if loan is overdue (`GET /api/pawn-loans/{id}/overdue`)

5. **Repayment Flow:**
   - Create repayment (`POST /api/pawn-repayments`)
   - Get repayment history for a loan (`GET /api/pawn-repayments/loan/{loanId}`)
   - Calculate repayment schedule (`GET /api/pawn-repayments/loan/{loanId}/schedule`)
   - Get total paid amount (`GET /api/pawn-repayments/loan/{loanId}/total-paid`)

6. **Reporting Flow:**
   - Get repayments by date range (`GET /api/pawn-repayments/by-date`)
   - Get daily collection report (`GET /api/pawn-repayments/daily-collection/{branchId}`)
   - Get customer repayment summary (`GET /api/pawn-repayments/customer/{customerId}/summary/{months}`)

## 10. Postman Collection Structure

### Recommended Folder Structure:
1. **Master Data**
   - Branches
   - Currencies
   - (If implemented) Payment Methods, Payment Types, Roles, Users

2. **Customer Management**
   - Create Customer
   - Get Customer
   - Update Customer
   - List Customers

3. **Collateral Management**
   - Create Pawn Item
   - Get Pawn Item
   - Update Pawn Item
   - List Pawn Items

4. **Loan Management**
   - Create Loan
   - Get Loan
   - Redeem Loan
   - Default Loan
   - List Loans

5. **Repayment Management**
   - Create Repayment
   - Get Repayment History
   - Calculate Schedule
   - Get Reports

6. **Reporting**
   - Date Range Reports
   - Daily Collection
   - Customer Summary

## 11. Important Notes for Testing

### Environment Variables:
- Set base URL as environment variable: `{{baseUrl}}` = `http://localhost:8080`
- Store created IDs (customerId, pawnItemId, loanId) as variables for chained requests

### Authentication:
- Currently no authentication implemented in controllers
- If authentication is added later, include Authorization headers

### Data Dependencies:
- Customer must exist before creating pawn item
- Pawn item must be AVAILABLE before creating loan
- Loan must be ACTIVE before creating repayment
- Branch and Currency must exist before creating loan

### Error Handling:
- Check for 400 Bad Request for validation errors
- Check for 404 Not Found for non-existent resources
- Check for 409 Conflict for duplicate data (unique constraints)

## 12. Module Status Summary

### Fully Implemented Modules (with Controllers):
1. Customer Management ✓
2. Pawn Item (Collateral) Management ✓
3. Pawn Loan Management ✓
4. Pawn Repayment Management ✓
5. Branch Management ✓
6. Currency Management ✓

### Module Analysis - Entities Without Controllers:

| Entity | Entity Status | Repository Status | Controller Status | Notes |
|--------|---------------|-------------------|-------------------|-------|
| **Loan Configuration (CfgLoan)** | Entity exists | No repository found | No controller | Configuration for loan parameters (interest rates, limits, etc.) |
| **Payment Method** | Entity exists | Repository exists (`PaymentMethodRepository`) | No controller | Master data for payment methods (Cash, Bank Transfer, etc.) |
| **Payment Type** | Entity exists | Repository exists (`PaymentTypeRepository`) | No controller | Master data for payment types (Full, Partial, Interest Only) |
| **Role** | Entity exists | No repository found | No controller | User roles and permissions |
| **User** | Entity exists | Repository exists (`UserRepository`) | No controller | System users with role and branch assignments |
| **Pawn Forfeit** | Entity exists | No repository found | No controller | Records for forfeited loans |

**Summary:**
- 6 entities have data models but lack REST API endpoints
- 3 entities have repositories (PaymentMethod, PaymentType, User)
- 3 entities lack repositories (CfgLoan, Role, PawnForfeit)
- All 6 entities need controller implementation for full CRUD operations

### Next Steps for Complete System:
1. Implement controllers for missing entities
2. Add authentication and authorization
3. Implement business logic for loan forfeiture
4. Add more reporting endpoints
5. Implement notification system

---

*Last Updated: 2026-02-23*
*For Postman testing, import this document as reference and create collections accordingly.*
