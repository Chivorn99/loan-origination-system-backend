# Frontend-Backend Alignment Guide for Pawn Loan Creation

## Overview
This document outlines the business rules and validation requirements that must be enforced consistently between the frontend (client-side) and backend (server-side) for the pawn loan creation flow.

## Business Rules (Source of Truth)

### Customer Rules
- Customer identified by **nationalId**
- **National ID Validation:** Must be exactly 9 digits and contain only numbers (e.g., "123456789")
- If nationalId exists → reuse existing customer
- If nationalId doesn't exist → create new customer (requires customer info)
- One customer can have multiple loans (no limit)

### Collateral Rules (CRITICAL)
- One collateral = ONE ACTIVE loan at a time
- Collateral statuses: `AVAILABLE`, `PAWNED`, `REDEEMED`, `FORFEITED`, `DELETED`
- If collateral status = `PAWNED` → block loan creation
- Same collateral cannot be reused while active

### Loan Rules
Loan requires:
- `nationalId` (required, exactly 9 digits, numbers only) - customer identification
- `pawnItemId` (required for existing collateral) OR new collateral data
- `loanAmount` (positive, ≤ 70% of collateral value)
- `interestRate` (positive)
- `dueDate` (required, must be today or in the future)
- `currencyId` (required)
- `branchId` (required)

## API Endpoints

### Create Full Loan (Single Request)
```
POST /api/pawn-loans/create-full
Content-Type: application/json

{
  "nationalId": "string (required, exactly 9 digits, numbers only)",
  "customerInfo": {
    "fullName": "string (required if new customer)",
    "phone": "string (required if new customer)",
    "address": "string (optional)"
  },
  "collateralInfo": {
    "pawnItemId": "number (optional - existing collateral)",
    "itemType": "string (required if new collateral)",
    "description": "string (optional)",
    "estimatedValue": "number (positive, required if new collateral)",
    "photoUrl": "string (optional)"
  },
  "loanInfo": {
    "currencyId": "number (required)",
    "branchId": "number (required)",
    "loanAmount": "number (positive, required)",
    "interestRate": "number (positive, required)",
    "dueDate": "string (ISO date, required, must be today or in the future)",
    "redemptionDeadline": "string (ISO date, optional)",
    "loanDurationDays": "number (min: 1, default: 30)",
    "gracePeriodDays": "number (min: 0, default: 7)",
    "storageFee": "number (positive, default: 0)",
    "penaltyRate": "number (positive, default: 0)",
    "paymentFrequency": "string (enum: ONE_TIME, MONTHLY, etc.)",
    "numberOfInstallments": "number (min: 1, default: 1)",
    "installmentAmount": "number (positive, optional)"
  }
}
```

### Create Loan (Existing Customer & Collateral)
```
POST /api/pawn-loans
Content-Type: application/json

{
  "nationalId": "string (required, exactly 9 digits, numbers only)",
  "pawnItemId": "number (required - existing collateral ID)",
  "currencyId": "number (required)",
  "branchId": "number (required)",
  "loanAmount": "number (positive, required)",
  "interestRate": "number (positive, required)",
  "dueDate": "string (ISO date, required, must be today or in the future)",
  "redemptionDeadline": "string (ISO date, optional)",
  "loanDurationDays": "number (min: 1, default: 30)",
  "gracePeriodDays": "number (min: 0, default: 7)",
  "storageFee": "number (positive, default: 0)",
  "penaltyRate": "number (positive, default: 0)",
  "paymentFrequency": "string (enum: ONE_TIME, MONTHLY, etc.)",
  "numberOfInstallments": "number (min: 1, default: 1)",
  "installmentAmount": "number (positive, optional)"
}
```

**Note:** This endpoint requires both customer and collateral to already exist in the system. The customer is identified by `nationalId` and the collateral by `pawnItemId`.

## Client-Side State Requirements

### Unified Form State
```typescript
interface LoanCreationState {
  customer: Customer | null;
  pawnItem: PawnItem | null;
  loanDetails: LoanDetails | null;
}

interface Customer {
  id?: number;
  nationalId: string;
  fullName: string;
  phone: string;
  address?: string;
}

interface PawnItem {
  id?: number;
  itemType: string;
  description?: string;
  estimatedValue: number;
  status: 'AVAILABLE' | 'PAWNED' | 'REDEEMED' | 'FORFEITED' | 'DELETED';
}

interface LoanDetails {
  currencyId: number;
  branchId: number;
  loanAmount: number;
  interestRate: number;
  dueDate: string; // ISO date
  // ... other fields
}
```

## Step Validation Requirements

### Step 1 — Customer
**Before moving to Step 2:**
- `nationalId` must be provided
- If customer exists with nationalId → proceed
- If customer doesn't exist → `customerInfo` must be complete:
  - `fullName` (required)
  - `phone` (required)
  - `address` (optional)

### Step 2 — Pawn Item
**Before moving to Step 3:**
- Either `existingPawnItemId` selected OR new collateral form completed
- **CRITICAL CHECK:** If selected pawn item has `status === "PAWNED"` → BLOCK NEXT STEP, show error
- New collateral requires:
  - `itemType` (required)
  - `estimatedValue` (positive, required)

### Step 3 — Loan Details
**Before moving to Step 4:**
- `currencyId` selected
- `branchId` selected
- `loanAmount` > 0
- `interestRate` >= 0
- `dueDate` exists
- **Client must calculate:** `totalDue = loanAmount + interest`
- Preview must match backend calculation

### Step 4 — Review & Confirm (CRITICAL)
**Before enabling "Confirm & Create Loan" button:**
```typescript
const canSubmitLoan = 
  state.customer != null &&
  state.pawnItem != null &&
  state.loanDetails != null &&
  state.loanDetails.loanAmount > 0 &&
  state.loanDetails.dueDate != null &&
  state.loanDetails.currencyId != null &&
  state.loanDetails.branchId != null &&
  state.pawnItem.status !== 'PAWNED';
```

## Client-Side Guards (Must Implement)

### 1. Customer Validation Guard
```typescript
function canProceedToStep2(state: LoanCreationState): boolean {
  if (!state.customer?.nationalId) return false;
  
  // If new customer, validate required fields
  if (!state.customer.id) {
    return !!state.customer.fullName && !!state.customer.phone;
  }
  
  return true;
}
```

### 2. Collateral Validation Guard
```typescript
function canProceedToStep3(state: LoanCreationState): boolean {
  if (!state.pawnItem) return false;
  
  // Block if pawned
  if (state.pawnItem.status === 'PAWNED') {
    return false;
  }
  
  // Validate new collateral if no ID
  if (!state.pawnItem.id) {
    return !!state.pawnItem.itemType && 
           !!state.pawnItem.estimatedValue &&
           state.pawnItem.estimatedValue > 0;
  }
  
  return true;
}
```

### 3. Loan Details Validation Guard
```typescript
function canProceedToStep4(state: LoanCreationState): boolean {
  if (!state.loanDetails) return false;
  
  return !!state.loanDetails.currencyId &&
         !!state.loanDetails.branchId &&
         !!state.loanDetails.loanAmount &&
         state.loanDetails.loanAmount > 0 &&
         !!state.loanDetails.interestRate &&
         state.loanDetails.interestRate >= 0 &&
         !!state.loanDetails.dueDate;
}
```

### 4. Final Submission Guard
```typescript
function canSubmitLoan(state: LoanCreationState): boolean {
  return canProceedToStep2(state) &&
         canProceedToStep3(state) &&
         canProceedToStep4(state) &&
         // Additional business rule: loanAmount ≤ 70% of collateral value
         (state.loanDetails!.loanAmount <= (state.pawnItem!.estimatedValue * 0.70));
}
```

## Data Consistency Checks

### 1. Total Due Calculation
Frontend must calculate total due exactly as backend:
```typescript
function calculateTotalDue(loanAmount: number, interestRate: number, storageFee: number = 0): number {
  const interestAmount = loanAmount * (interestRate / 100);
  return loanAmount + interestAmount + storageFee;
}
```

### 2. Preview Data
Step 4 review screen must display EXACT data that will be sent to API:
- No recalculation after confirmation
- UI total == backend total
- All values come ONLY from client state

## Validation Tests (Frontend Must Implement)

### Mandatory Test Cases
1. **Cannot proceed without customer**
   - Empty nationalId → block Step 2
   - New customer without name/phone → block Step 2

2. **Cannot pawn already pawned item**
   - Selected pawn item with status "PAWNED" → block Step 3, show error

3. **Cannot confirm without loan amount**
   - Empty loanAmount → disable confirm button

4. **Cannot confirm with empty due date**
   - Empty dueDate → disable confirm button

5. **Multiple loans allowed for same customer**
   - Same nationalId with different collateral → allowed

6. **Same collateral cannot be reused**
   - Attempt to use pawned collateral → blocked by backend

7. **Loan amount limit (70% rule)**
   - loanAmount > 70% of collateral value → show error, block submission

## UI Alignment Requirements

### Review Screen Display
**Customer Section:**
- Full Name
- Phone
- ID Number (nationalId)
- Address

**Pawn Item Section:**
- Type
- Description
- Estimated Value

**Loan Terms Section:**
- Loan Amount
- Interest Rate
- Loan Date (current date)
- Due Date
- Total Due at Maturity (calculated)

## Error Handling

### Backend Error Codes (Map to Frontend Messages)
- `CUSTOMER_NOT_FOUND`: "Customer with national ID {id} not found"
- `CUSTOMER_NAME_REQUIRED`: "Full name is required for new customer"
- `CUSTOMER_PHONE_REQUIRED`: "Phone is required for new customer"
- **National ID Validation Errors:**
  - `INVALID_NATIONAL_ID_FORMAT`: "National ID must be exactly 9 digits and contain only numbers"
- **Due Date Validation Errors:**
  - `INVALID_DUE_DATE`: "Due date must be today or in the future"
- `COLLATERAL_NOT_FOUND`: "Collateral with ID {id} not found"
- `COLLATERAL_NOT_AVAILABLE`: "Collateral is not available for pawn (status: {status})"
- `COLLATERAL_OWNERSHIP_MISMATCH`: "Collateral does not belong to customer"
- `LOAN_AMOUNT_EXCEEDS_LIMIT`: "Loan amount exceeds maximum allowed (70% of collateral value)"
- `BRANCH_NOT_FOUND`: "Branch not found"
- `CURRENCY_NOT_FOUND`: "Currency not found"

## Success Condition
The flow must guarantee:
```
User cannot reach Confirm button with invalid data.
```
If user reaches confirmation → API request must succeed (backend validation passes).

## Implementation Notes

### Frontend Responsibilities
1. Implement step-by-step validation
2. Maintain unified form state
3. Calculate totals matching backend logic
4. Display preview matching API payload
5. Handle errors gracefully

### Backend Guarantees
1. All business rules enforced
2. Consistent validation with frontend
3. Clear error messages
4. Data integrity maintained

### Testing Strategy
1. Unit tests for each validation guard
2. Integration tests for API calls
3. End-to-end tests for complete flow
4. Test edge cases (pawned collateral, max loan amount, etc.)

## Version History
- v1.0: Initial alignment guide
- Updated: 2026-02-27