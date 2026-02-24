# Branch Module - Postman Testing Guide

## Base URL
```
http://localhost:8080/api/branches
```

## Mock Data for Testing

### 1. Branch Entities (for database seeding)

| ID | Name | Address | Phone | Status |
|----|------|---------|-------|--------|
| 1 | Phnom Penh Main Branch | Street 123, Phnom Penh, Cambodia | +855123456789 | ACTIVE |
| 2 | Siem Reap Branch | Street 456, Siem Reap, Cambodia | +855987654321 | ACTIVE |
| 3 | Sihanoukville Branch | Street 789, Sihanoukville, Cambodia | +855112233445 | INACTIVE |
| 4 | Battambang Branch | Street 101, Battambang, Cambodia | +855556677889 | ACTIVE |
| 5 | Kampong Cham Branch | Street 202, Kampong Cham, Cambodia | +855998877665 | ACTIVE |

### 2. Request Body Examples

#### Create Branch Request (POST)
```json
{
  "name": "New Test Branch",
  "address": "Test Street 123, Phnom Penh",
  "phone": "+855123456789",
  "status": "ACTIVE"
}
```

#### Update Branch Request (PUT)
```json
{
  "name": "Updated Branch Name",
  "address": "Updated Address, Phnom Penh",
  "phone": "+855987654321",
  "status": "ACTIVE"
}
```

#### Partial Update Request (PATCH)
```json
{
  "phone": "+855112233445",
  "status": "INACTIVE"
}
```

## API Endpoints for Testing

### 1. Create Branch
- **Method**: POST
- **URL**: `{{baseUrl}}`
- **Body**: 
```json
{
  "name": "Test Branch 1",
  "address": "123 Test Street",
  "phone": "+855123456789",
  "status": "ACTIVE"
}
```
- **Expected Response (201 Created)**:
```json
{
  "success": true,
  "message": "Branch created successfully",
  "data": {
    "id": 1,
    "name": "Test Branch 1",
    "address": "123 Test Street",
    "phone": "+855123456789",
    "status": "ACTIVE"
  }
}
```

### 2. Get Branch by ID
- **Method**: GET
- **URL**: `{{baseUrl}}/1`
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "name": "Test Branch 1",
    "address": "123 Test Street",
    "phone": "+855123456789",
    "status": "ACTIVE"
  }
}
```

### 3. Get All Branches (Paginated)
- **Method**: GET
- **URL**: `{{baseUrl}}?page=0&size=10&sortBy=name&direction=asc`
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Test Branch 1",
        "address": "123 Test Street",
        "phone": "+855123456789",
        "status": "ACTIVE"
      },
      {
        "id": 2,
        "name": "Test Branch 2",
        "address": "456 Another Street",
        "phone": "+855987654321",
        "status": "ACTIVE"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalElements": 2,
    "totalPages": 1,
    "last": true,
    "size": 10,
    "number": 0,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "first": true,
    "numberOfElements": 2,
    "empty": false
  }
}
```

### 4. Get Branches by Status
- **Method**: GET
- **URL**: `{{baseUrl}}?status=ACTIVE&page=0&size=10`
- **Expected Response**: List of only ACTIVE branches

### 5. Update Branch (Full Update)
- **Method**: PUT
- **URL**: `{{baseUrl}}/1`
- **Body**:
```json
{
  "name": "Updated Branch Name",
  "address": "Updated Address",
  "phone": "+855987654321",
  "status": "ACTIVE"
}
```
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Branch updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Branch Name",
    "address": "Updated Address",
    "phone": "+855987654321",
    "status": "ACTIVE"
  }
}
```

### 6. Partial Update Branch
- **Method**: PATCH
- **URL**: `{{baseUrl}}/1`
- **Body**:
```json
{
  "phone": "+855112233445"
}
```
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Branch updated successfully",
  "data": {
    "id": 1,
    "name": "Updated Branch Name",
    "address": "Updated Address",
    "phone": "+855112233445",
    "status": "ACTIVE"
  }
}
```

### 7. Check Branch Exists by Name
- **Method**: GET
- **URL**: `{{baseUrl}}/exists/Test Branch 1`
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Success",
  "data": true
}
```

### 8. Soft Delete Branch
- **Method**: DELETE
- **URL**: `{{baseUrl}}/1`
- **Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Branch deleted successfully",
  "data": null
}
```

## Error Scenarios to Test

### 1. Create Branch with Duplicate Name
- **Method**: POST
- **URL**: `{{baseUrl}}`
- **Body**: Use same name as existing branch
- **Expected Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Branch with this name already exists",
  "data": null
}
```

### 2. Get Non-Existent Branch
- **Method**: GET
- **URL**: `{{baseUrl}}/999`
- **Expected Response (404 Not Found)**:
```json
{
  "success": false,
  "message": "Branch with ID 999 not found",
  "data": null
}
```

### 3. Update with Duplicate Name
- **Method**: PUT
- **URL**: `{{baseUrl}}/2`
- **Body**: Use name of branch 1
- **Expected Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Branch with this name already exists",
  "data": null
}
```

### 4. Invalid Request Body
- **Method**: POST
- **URL**: `{{baseUrl}}`
- **Body**:
```json
{
  "name": "",
  "address": "Test",
  "phone": "invalid-phone"
}
```
- **Expected Response (400 Bad Request)**: Validation errors

## Postman Collection Variables

```json
{
  "baseUrl": "http://localhost:8080/api/branches",
  "createdBranchId": "{{response.data.id}}"
}
```

## Testing Sequence

1. **Create Test Data**: Use POST to create 3-5 branches
2. **Retrieve Validation**: Test GET endpoints with different parameters
3. **Update Operations**: Test PUT and PATCH with valid and invalid data
4. **Business Rules**: Test duplicate name validation
5. **Delete Operations**: Test soft delete and verify status change
6. **Edge Cases**: Test pagination, filtering, and error scenarios

## Sample Test Script (Postman Tests)

```javascript
// Test for successful branch creation
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has success true", function () {
    const response = pm.response.json();
    pm.expect(response.success).to.be.true;
});

pm.test("Branch has ID", function () {
    const response = pm.response.json();
    pm.expect(response.data.id).to.be.a('number');
});

// Store branch ID for later tests
if (pm.response.code === 201) {
    const response = pm.response.json();
    pm.environment.set("branchId", response.data.id);
}
```

## Quick Test Commands (cURL)

```bash
# Create branch
curl -X POST http://localhost:8080/api/branches \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Branch","address":"Test Address","phone":"+855123456789","status":"ACTIVE"}'

# Get branch by ID
curl -X GET http://localhost:8080/api/branches/1

# Get all branches
curl -X GET "http://localhost:8080/api/branches?page=0&size=10&sortBy=name&direction=asc"

# Update branch
curl -X PUT http://localhost:8080/api/branches/1 \
  -H "Content-Type: application/json" \
  -d '{"name":"Updated Name","address":"Updated Address","phone":"+855987654321","status":"ACTIVE"}'

# Delete branch
curl -X DELETE http://localhost:8080/api/branches/1