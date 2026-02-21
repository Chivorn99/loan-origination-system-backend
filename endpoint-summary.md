# Loan Origination System - Endpoint Summary by Module and Entity

## Overview
This document summarizes all REST API endpoints in the Loan Origination System, organized by module and entity. Each endpoint includes HTTP method, URL, description, request/response structures, and mock data recommendations.

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
| GET | `/api/customers` | Get all customers (paginated) | Query params: `page`, `size`, `sortBy`, `direction` | `Page<Customer>` |
| GET | `/api/customers/` | Get customers by status | Query params: `status`, `page`, `size` | `Page<Customer>` |
| DELETE | `/api/customers/{id}` | Soft delete customer | - | Success message |
| GET | `/api/customers/{id}/has-active-loans` | Check if customer has active loans | - | `Boolean` |

#### Data Structures:
- **CustomerRequest**: `{fullName: string, phone: string, idNumber: string, address: string}`
- **Customer**: `{id: long, fullName: string, phone: string, idNumber: string, address: string, status: CustomerStatus, createdAt: LocalDateTime, updatedAt: LocalDateTime, deletedAt: LocalDateTime}`
- **CustomerStatus**: `ACTIVE`, `INACTIVE`, `DELETED`

#### Mock Data Recommendations:
```json
{
  "fullName": "John Doe",
  "phone": "+85512345678",
  "idNumber": "123456789012",
  "address": "Phnom Penh, Cambodia"
}
```

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

#### Mock Data Recommendations:
```json
{
  "customerId": 1,
  "itemType": "Gold Necklace",
  "description": "24K gold necklace with pendant",
  "estimatedValue": 1500.00,
  "photoUrl": "https://example.com/photo.jpg"
}
```

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
- **LoanStatus**: (Check enum - likely includes `ACTIVE`, `REDEEMED`, `DEFAULTED`, etc.)

#### Mock Data Recommendations:
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

#### Mock Data Recommendations:
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
| GET | `/api/branches` | Get all branches (paginated) | Query params: `page`, `size`, `sortBy`, `direction` | `Page<Branch>` |
| GET | `/api/branches` | Get branches by status | Query params: `status`, `page`, `size` | `Page<Branch>` |
| DELETE | `/api/branches/{id}` | Soft delete branch (set status to INACTIVE) | - | Success message |
| GET | `/api/branches/exists/{name}` | Check if branch exists by name | - | `Boolean` |

#### Data Structures:
- **BranchRequest**: `{name: string, address: string, phone: string, status: string}`
- **BranchPatchRequest**: `{name: string, address: string, phone: string, status: string}`
- **Branch**: `{id: long, name: string, address: string, phone: string, status: string}`

#### Mock Data Recommendations:
```json
{
  "name": "Phnom Penh Main Branch",
  "address": "Street 123, Phnom Penh, Cambodia",
  "phone": "+855123456789",
  "status": "ACTIVE"
}
```

## 6. Currency Management Module

### Entity: Currency
**Base URL:** `/api/currencies`

#### Endpoints:

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/currencies` | Create new currency | `CurrencyRequest` | `Currency` |
| GET | `/api/currencies/{id}` | Get currency by ID | - | `Currency` |
| PUT | `/api/currencies/{id}` | Update currency | `CurrencyRequest` | `Currency` |
| PATCH | `/api/currencies/{id}` | Partial update currency | `CurrencyPatchRequest` | `Currency` |
| GET | `/api/currencies` | Get all currencies (paginated) | Query params: `page`, `size`, `sortBy`, `direction`, `status` | `Page<Currency>` |
| DELETE | `/api/currencies/{id}` | Soft delete currency (set status to INACTIVE) | - | Success message |
| GET | `/api/currencies/exists/{code}` | Check if currency exists by code | - | `Boolean` |

#### Data Structures:
- **CurrencyRequest**: `{code: string, name: string, symbol: string, decimalPlace: integer, status: string}`
- **CurrencyPatchRequest**: `{code: string, name: string, symbol: string, decimalPlace: integer, status: string}`
- **Currency**: `{id: long, code: string, name: string, symbol: string, decimalPlace: integer, status: string}`

#### Mock Data Recommendations:
```json
{
  "code": "USD",
  "name": "US Dollar",
  "symbol": "$",
  "decimalPlace": 2,
  "status": "ACTIVE"
}
```

## 7. Master Data Modules

Based on repository files, the system includes these master entities (controllers may not be implemented yet):
- **PaymentMethod**: `PaymentMethodRepository`
- **PaymentType**: `PaymentTypeRepository`
- **User**: `UserRepository`
- **Role**: `RoleRepository`

These likely support dropdowns and reference data for the main modules.

## Mock Data Preparation Guidelines

### 1. Customer Data
- Create 10-20 customers with realistic Cambodian names and ID numbers
- Include mix of statuses: 70% ACTIVE, 20% INACTIVE, 10% DELETED
- Ensure unique ID numbers

### 2. Collateral Items
- Create 15-30 pawn items across different customers
- Item types: Gold jewelry, electronics, vehicles, documents
- Status distribution: 40% AVAILABLE, 40% PAWNED, 10% REDEEMED, 10% FORFEITED

### 3. Loans
- Create 20-40 loans with realistic amounts (100-5000 USD equivalent)
- Link to existing customers and collateral items
- Include various statuses: ACTIVE, REDEEMED, DEFAULTED
- Set realistic due dates (30-180 days from creation)

### 4. Repayments
- Create repayments for 60% of active loans
- Include partial and full repayments
- Create repayment schedules for testing
- Include date ranges for reporting tests

### 5. Master Data
- Create 3-5 branches (different locations)
- Create 2-3 currencies (USD, KHR, THB)
- Create 4-5 payment methods (Cash, Bank Transfer, Mobile Payment)
- Create 3-4 payment types (Full, Partial, Interest Only)
- Create 5-10 users with different roles

## Testing Scenarios

1. **Customer Registration Flow**: Create customer → verify creation → update details → check status
2. **Loan Origination Flow**: Customer exists → collateral available → create loan → verify loan code
3. **Repayment Flow**: Active loan → make repayment → verify totals → check remaining balance
4. **Reporting Flow**: Date range queries → status filters → pagination tests
5. **Business Rules**: Check collateral availability before loan, validate payment amounts, prevent duplicate ID numbers

## Notes for Mock Data Generation

1. Maintain referential integrity (customer IDs must exist before creating their loans)
2. Respect business rules (can't create loan for deleted customer)
3. Include edge cases: zero values, maximum amounts, date boundaries
4. Create data for all status values to test filtering endpoints
5. Generate realistic timestamps (createdAt, updatedAt) with logical sequences