# Currency Module - Loan Origination System

## Overview
The Currency module provides master data management for currencies used in the loan origination system. It supports creating, reading, updating, and deleting currency records with proper validation and business rules.

## Entity Structure

### Currency Model
**Package:** `com.example.loan_origination_system.model.master.Currency`

```java
@Entity
@Table(name = "m_currency")
@Data
public class Currency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;          // Currency code (e.g., "USD", "KHR")
    
    private String name;          // Currency name (e.g., "US Dollar")
    private String symbol;        // Currency symbol (e.g., "$")
    private Integer decimalPlace = 2;  // Decimal places (default: 2)
    private String status = "ACTIVE";  // Status: ACTIVE, INACTIVE
}
```

### Database Table
```sql
CREATE TABLE m_currency (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(10) UNIQUE NOT NULL,
    name VARCHAR(100),
    symbol VARCHAR(10),
    decimal_place INT DEFAULT 2,
    status VARCHAR(20) DEFAULT 'ACTIVE'
);
```

## API Endpoints

### Base URL
```
/api/currencies
```

### 1. Create Currency
**POST** `/api/currencies`

Creates a new currency record.

**Request Body:**
```json
{
  "code": "USD",
  "name": "US Dollar",
  "symbol": "$",
  "decimalPlace": 2,
  "status": "ACTIVE"
}
```

**Validation Rules:**
- `code`: Required, must be unique
- `name`: Required
- `decimalPlace`: Optional, defaults to 2
- `status`: Optional, defaults to "ACTIVE"

**Response:**
```json
{
  "success": true,
  "message": "Currency created successfully",
  "data": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "decimalPlace": 2,
    "status": "ACTIVE"
  }
}
```

**Error Responses:**
- `400 Bad Request`: Validation errors
- `409 Conflict`: Currency code already exists

### 2. Get Currency by ID
**GET** `/api/currencies/{id}`

Retrieves a currency by its ID.

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar",
    "symbol": "$",
    "decimalPlace": 2,
    "status": "ACTIVE"
  }
}
```

**Error Responses:**
- `404 Not Found`: Currency not found

### 3. Update Currency (Full Update)
**PUT** `/api/currencies/{id}`

Performs a full update of a currency record.

**Request Body:** Same as Create Currency

**Response:**
```json
{
  "success": true,
  "message": "Currency updated successfully",
  "data": {
    "id": 1,
    "code": "USD",
    "name": "US Dollar Updated",
    "symbol": "US$",
    "decimalPlace": 2,
    "status": "ACTIVE"
  }
}
```

**Business Rules:**
- Validates duplicate currency code (excluding current record)
- All fields are updated with provided values

### 4. Partial Update Currency
**PATCH** `/api/currencies/{id}`

Performs a partial update of a currency record.

**Request Body:**
```json
{
  "name": "Updated Currency Name",
  "status": "INACTIVE"
}
```

**Response:** Same as full update

**Note:** Only provided fields are updated; others remain unchanged.

### 5. List Currencies
**GET** `/api/currencies`

Retrieves a paginated list of currencies with optional filtering.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 10)
- `sortBy`: Field to sort by (default: "code")
- `direction`: Sort direction - "asc" or "desc" (default: "asc")
- `status`: Filter by status (optional)

**Examples:**
```
GET /api/currencies?page=0&size=5&sortBy=name&direction=asc
GET /api/currencies?status=ACTIVE&page=0&size=10
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "id": 1,
        "code": "USD",
        "name": "US Dollar",
        "symbol": "$",
        "decimalPlace": 2,
        "status": "ACTIVE"
      },
      {
        "id": 2,
        "code": "KHR",
        "name": "Cambodian Riel",
        "symbol": "៛",
        "decimalPlace": 0,
        "status": "ACTIVE"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": { "sorted": true, "unsorted": false, "empty": false }
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "size": 10,
    "number": 0,
    "sort": { "sorted": true, "unsorted": false, "empty": false },
    "first": true,
    "numberOfElements": 2,
    "empty": false
  }
}
```

### 6. Delete Currency (Soft Delete)
**DELETE** `/api/currencies/{id}`

Performs a soft delete by setting the currency status to "INACTIVE".

**Response:**
```json
{
  "success": true,
  "message": "Currency deleted successfully",
  "data": null
}
```

**Note:** This is a soft delete; the record remains in the database with status "INACTIVE".

### 7. Check Currency Code Exists
**GET** `/api/currencies/exists/{code}`

Checks if a currency code already exists.

**Example:**
```
GET /api/currencies/exists/USD
```

**Response:**
```json
{
  "success": true,
  "data": true
}
```

## Data Transfer Objects (DTOs)

### CurrencyRequest
Used for create and full update operations.

```java
@Data
public class CurrencyRequest {
    @NotBlank(message = "Currency code is required")
    private String code;
    
    @NotBlank(message = "Currency name is required")
    private String name;
    
    private String symbol;
    private Integer decimalPlace = 2;
    private String status = "ACTIVE";
}
```

### CurrencyPatchRequest
Used for partial update operations.

```java
@Data
public class CurrencyPatchRequest {
    private String code;
    private String name;
    private String symbol;
    private Integer decimalPlace;
    private String status;
}
```

## Service Layer

### CurrencyService
**Package:** `com.example.loan_origination_system.service.CurrencyService`

**Key Methods:**
- `createCurrency(CurrencyRequest request)` - Creates new currency with duplicate validation
- `updateCurrency(Long id, CurrencyRequest request)` - Full update with validation
- `patchCurrency(Long id, CurrencyPatchRequest patch)` - Partial update
- `getCurrencyById(Long id)` - Retrieves currency by ID with error handling
- `getAllCurrencies(Pageable pageable)` - Paginated listing
- `getCurrenciesByStatus(String status, Pageable pageable)` - Filter by status
- `deleteCurrency(Long id)` - Soft delete (sets status to INACTIVE)
- `existsByCode(String code)` - Checks for duplicate currency codes

**Business Rules:**
1. Currency code must be unique
2. Default decimal places: 2
3. Default status: ACTIVE
4. Soft delete only (status change to INACTIVE)

## Repository Layer

### CurrencyRepository
**Package:** `com.example.loan_origination_system.repository.CurrencyRepository`

**Custom Query Methods:**
```java
boolean existsByCode(String code);
boolean existsByCodeAndIdNot(String code, Long excludeId);
Page<Currency> findByStatus(String status, Pageable pageable);
```

## Error Handling

### Business Exceptions
- **CURRENCY_NOT_FOUND**: When currency with given ID doesn't exist
- **CURRENCY_CODE_DUPLICATE**: When trying to create/update with duplicate currency code

### Example Error Response:
```json
{
  "success": false,
  "message": "Currency with this code already exists",
  "errorCode": "CURRENCY_CODE_DUPLICATE",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Testing Guidelines

### Mock Data Examples
```json
// US Dollar
{
  "code": "USD",
  "name": "US Dollar",
  "symbol": "$",
  "decimalPlace": 2,
  "status": "ACTIVE"
}

// Cambodian Riel
{
  "code": "KHR",
  "name": "Cambodian Riel",
  "symbol": "៛",
  "decimalPlace": 0,
  "status": "ACTIVE"
}

// Thai Baht
{
  "code": "THB",
  "name": "Thai Baht",
  "symbol": "฿",
  "decimalPlace": 2,
  "status": "ACTIVE"
}
```

### Test Scenarios
1. **Create Currency**
   - Success: Valid currency data
   - Failure: Duplicate currency code
   - Failure: Missing required fields

2. **Update Currency**
   - Success: Valid update
   - Failure: Duplicate code (different record)
   - Success: Partial update

3. **List Currencies**
   - Success: Pagination
   - Success: Filter by status
   - Success: Sorting

4. **Delete Currency**
   - Success: Soft delete (status becomes INACTIVE)
   - Failure: Currency not found

5. **Check Exists**
   - Success: Returns true for existing code
   - Success: Returns false for non-existing code

## Integration with Other Modules

### Loan Module Integration
Currencies are referenced in:
- `PawnLoan` entity (currencyId foreign key)
- `PawnRepayment` entity (currencyId foreign key)

### Usage in Loan Creation
When creating a loan, the currency ID must reference an ACTIVE currency:
```json
{
  "customerId": 1,
  "pawnItemId": 1,
  "currencyId": 1,  // References Currency entity
  "branchId": 1,
  "loanAmount": 1000.00,
  "interestRate": 5.0,
  "dueDate": "2024-12-31"
}
```

## Best Practices

1. **Currency Codes**: Use ISO 4217 codes when possible (USD, KHR, THB)
2. **Decimal Places**: Set appropriate decimal places (0 for KHR, 2 for USD/THB)
3. **Status Management**: Use soft delete (INACTIVE) instead of hard delete
4. **Validation**: Always validate duplicate codes before create/update
5. **Pagination**: Use pagination for listing endpoints

## Related Files

- **Model**: [`src/main/java/com/example/loan_origination_system/model/master/Currency.java`](src/main/java/com/example/loan_origination_system/model/master/Currency.java)
- **Controller**: [`src/main/java/com/example/loan_origination_system/controller/CurrencyController.java`](src/main/java/com/example/loan_origination_system/controller/CurrencyController.java)
- **Service**: [`src/main/java/com/example/loan_origination_system/service/CurrencyService.java`](src/main/java/com/example/loan_origination_system/service/CurrencyService.java)
- **Repository**: [`src/main/java/com/example/loan_origination_system/repository/CurrencyRepository.java`](src/main/java/com/example/loan_origination_system/repository/CurrencyRepository.java)
- **DTOs**: [`src/main/java/com/example/loan_origination_system/dto/CurrencyRequest.java`](src/main/java/com/example/loan_origination_system/dto/CurrencyRequest.java) and [`CurrencyPatchRequest.java`](src/main/java/com/example/loan_origination_system/dto/CurrencyPatchRequest.java)

## API Testing Examples

### Using cURL
```bash
# Create currency
curl -X POST http://localhost:8080/api/currencies \
  -H "Content-Type: application/json" \
  -d '{"code":"USD","name":"US Dollar","symbol":"$","decimalPlace":2}'

# Get currency by ID
curl -X GET http://localhost:8080/api/currencies/1

# List currencies
curl -X GET "http://localhost:8080/api/currencies?page=0&size=10&sortBy=code"

# Update currency
curl -X PUT http://localhost:8080/api/currencies/1 \
  -H "Content-Type: application/json" \
  -d '{"code":"USD","name":"US Dollar Updated","symbol":"US$"}'

# Delete currency
curl -X DELETE http://localhost:8080/api/currencies/1
```

### Using Postman
1. Import the Postman collection from `branch-postman-testing.md`
2. Use the Currency endpoints with the provided examples
3. Test validation by sending invalid data

## Version History
- **v1.0.0** (2024-01-15): Initial implementation with full CRUD operations
- Features: Create, Read, Update, Delete, List with pagination, Duplicate validation