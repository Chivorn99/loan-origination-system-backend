# Testing API Endpoints - Loan Origination System

This guide provides comprehensive instructions for testing the API endpoints of the Loan Origination System backend.

## Table of Contents
1. [Project Overview](#project-overview)
2. [Testing Environment Setup](#testing-environment-setup)
3. [Running Tests](#running-tests)
4. [API Endpoints Reference](#api-endpoints-reference)
5. [Manual Testing with cURL](#manual-testing-with-curl)
6. [Manual Testing with Postman](#manual-testing-with-postman)
7. [Automated Testing with JUnit](#automated-testing-with-junit)
8. [Integration Testing](#integration-testing)
9. [Test Data Setup](#test-data-setup)
10. [Common Testing Scenarios](#common-testing-scenarios)
11. [Troubleshooting](#troubleshooting)

## Project Overview

The Loan Origination System is a Spring Boot application with the following key components:

- **Customer Management**: Create, read, update, and delete customer records
- **Collateral (Pawn Item) Management**: Manage items used as collateral for loans
- **Loan Management**: Create and manage pawn loans with status tracking

**Technology Stack:**
- Java 21
- Spring Boot 4.0.1
- Spring Data JPA
- PostgreSQL (production), H2 (testing)
- Maven build system
- JUnit 5 for testing

## Testing Environment Setup

### Prerequisites
- Java 21 or later
- Maven 3.6+
- Git
- (Optional) PostgreSQL for integration testing

### Configuration

The project includes two configuration profiles:

1. **Main configuration** (`application.properties`): Uses PostgreSQL with environment variables
2. **Test configuration** (`application-test-h2.properties`): Uses in-memory H2 database

To run tests, no additional setup is required as the test configuration automatically uses H2.

### Database Setup for Testing

Tests use an in-memory H2 database with the following properties:
- URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)
- H2 Console: Enabled at `/h2-console` (when running tests with web environment)

## Running Tests

### Run All Tests
```bash
mvn test
```

### Run Specific Test Class
```bash
mvn test -Dtest=CustomerControllerTest
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

### Run Integration Tests
```bash
mvn verify -P integration-test
```

### Test Output Location
- Test reports: `target/surefire-reports/`
- Coverage reports: `target/site/jacoco/` (if Jacoco configured)

## API Endpoints Reference

### Base URL
- Local development: `http://localhost:8080`
- Test environment: `http://localhost:8080`

### Customer Endpoints (`/api/customers`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/customers` | Create new customer | `CustomerRequest` JSON | 201 Created with customer |
| GET | `/api/customers/{id}` | Get customer by ID | - | 200 OK with customer |
| GET | `/api/customers/by-id-number/{idNumber}` | Get customer by national ID | - | 200 OK with customer |
| PUT | `/api/customers/{id}` | Update customer | `CustomerRequest` JSON | 200 OK with updated customer |
| GET | `/api/customers` | Get all customers (paginated) | Query params: page, size, sortBy, direction | 200 OK with page of customers |
| GET | `/api/customers/status/{status}` | Get customers by status | Query params: page, size | 200 OK with page of customers |
| DELETE | `/api/customers/{id}` | Soft delete customer | - | 200 OK with success message |
| GET | `/api/customers/{id}/has-active-loans` | Check if customer has active loans | - | 200 OK with boolean |

### Pawn Item Endpoints (`/api/pawn-items`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-items` | Create new collateral item | `PawnItemRequest` JSON | 201 Created with pawn item |
| GET | `/api/pawn-items/{id}` | Get collateral by ID | - | 200 OK with pawn item |
| GET | `/api/pawn-items/{id}/active` | Get active collateral | - | 200 OK with pawn item |
| PUT | `/api/pawn-items/{id}` | Update collateral item | `PawnItemRequest` JSON | 200 OK with updated pawn item |
| GET | `/api/pawn-items` | Get all items (paginated) | Query params: page, size, sortBy, direction | 200 OK with page of items |
| GET | `/api/pawn-items/customer/{customerId}` | Get items by customer ID | - | 200 OK with list of items |
| GET | `/api/pawn-items/status/{status}` | Get items by status | Query params: page, size | 200 OK with page of items |
| DELETE | `/api/pawn-items/{id}` | Soft delete collateral | - | 200 OK with success message |
| GET | `/api/pawn-items/{id}/available` | Check if collateral is available | - | 200 OK with boolean |

### Pawn Loan Endpoints (`/api/pawn-loans`)

| Method | Endpoint | Description | Request Body | Response |
|--------|----------|-------------|--------------|----------|
| POST | `/api/pawn-loans` | Create new loan | `PawnLoanRequest` JSON | 201 Created with loan |
| GET | `/api/pawn-loans/{id}` | Get loan by ID | - | 200 OK with loan |
| GET | `/api/pawn-loans/code/{loanCode}` | Get loan by code | - | 200 OK with loan |
| GET | `/api/pawn-loans` | Get all loans (paginated) | Query params: page, size, sortBy, direction | 200 OK with page of loans |
| GET | `/api/pawn-loans/customer/{customerId}` | Get loans by customer ID | - | 200 OK with list of loans |
| GET | `/api/pawn-loans/status/{status}` | Get loans by status | Query params: page, size | 200 OK with page of loans |
| POST | `/api/pawn-loans/{id}/redeem` | Redeem a loan | - | 200 OK with redeemed loan |
| POST | `/api/pawn-loans/{id}/default` | Mark loan as defaulted | - | 200 OK with defaulted loan |
| POST | `/api/pawn-loans/calculate-total` | Calculate total payable | Query params: principalAmount, interestRate | 200 OK with calculated amount |
| GET | `/api/pawn-loans/{id}/overdue` | Check if loan is overdue | - | 200 OK with boolean |

## Manual Testing with cURL

### Start the Application
```bash
mvn spring-boot:run
```

### Example cURL Commands

#### 1. Create a Customer
```bash
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "fullName": "John Doe",
    "phone": "+85512345678",
    "idNumber": "ID123456789",
    "address": "Phnom Penh, Cambodia"
  }'
```

#### 2. Get Customer by ID
```bash
curl -X GET http://localhost:8080/api/customers/1
```

#### 3. Create a Pawn Item
```bash
curl -X POST http://localhost:8080/api/pawn-items \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "itemType": "GOLD_NECKLACE",
    "description": "24K gold necklace with pendant",
    "estimatedValue": 1500.00,
    "photoUrl": "https://example.com/necklace.jpg"
  }'
```

#### 4. Create a Pawn Loan
```bash
curl -X POST http://localhost:8080/api/pawn-loans \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "pawnItemId": 1,
    "currencyId": 1,
    "branchId": 1,
    "loanAmount": 1000.00,
    "interestRate": 5.0,
    "dueDate": "2024-12-31"
  }'
```

#### 5. Get Paginated Results
```bash
curl -X GET "http://localhost:8080/api/customers?page=0&size=5&sortBy=createdAt&direction=desc"
```

## Manual Testing with Postman

### Postman Collection Setup
1. Create a new collection named "Loan Origination System"
2. Set base URL variable: `{{baseUrl}}` = `http://localhost:8080`
3. Create folders for each resource type: Customers, Pawn Items, Pawn Loans

### Request Examples

#### Customer Creation Request
- **Method**: POST
- **URL**: `{{baseUrl}}/api/customers`
- **Headers**: `Content-Type: application/json`
- **Body** (raw JSON):
```json
{
  "fullName": "Jane Smith",
  "phone": "+85598765432",
  "idNumber": "ID987654321",
  "address": "Siem Reap, Cambodia"
}
```

#### Testing Pagination
- **Method**: GET
- **URL**: `{{baseUrl}}/api/customers`
- **Params**:
  - `page`: 0
  - `size`: 10
  - `sortBy`: createdAt
  - `direction`: desc

### Environment Variables in Postman
Create an environment with:
- `baseUrl`: `http://localhost:8080`
- `customerId`: (dynamically set from response)
- `pawnItemId`: (dynamically set from response)
- `loanId`: (dynamically set from response)

### Test Scripts in Postman
Add tests to verify responses:
```javascript
// Test for successful customer creation
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has success message", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.message).to.include("created successfully");
});

// Set environment variable for created customer
const jsonData = pm.response.json();
pm.environment.set("customerId", jsonData.data.id);
```

## Automated Testing with JUnit

### Test Structure
The project follows standard Spring Boot testing patterns:

```
src/test/java/com/example/loan_origination_system/
├── LoanOriginationSystemApplicationTests.java
└── controller/
    ├── CustomerControllerTest.java
    ├── PawnItemControllerTest.java
    └── PawnLoanControllerTest.java
```

### Creating Controller Tests

Example test class structure for `CustomerControllerTest`:

```java
package com.example.loan_origination_system.controller;

import com.example.loan_origination_system.dto.CustomerRequest;
import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CustomerService customerService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private Customer testCustomer;
    
    @BeforeEach
    void setUp() {
        testCustomer = new Customer();
        testCustomer.setId(1L);
        testCustomer.setFullName("John Doe");
        testCustomer.setPhone("+85512345678");
        testCustomer.setIdNumber("ID123456789");
        testCustomer.setAddress("Phnom Penh, Cambodia");
    }
    
    @Test
    void createCustomer_ValidRequest_ReturnsCreated() throws Exception {
        CustomerRequest request = new CustomerRequest();
        request.setFullName("John Doe");
        request.setPhone("+85512345678");
        request.setIdNumber("ID123456789");
        request.setAddress("Phnom Penh, Cambodia");
        
        when(customerService.createCustomer(any(Customer.class)))
            .thenReturn(testCustomer);
        
        mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Customer created successfully"))
                .andExpect(jsonPath("$.data.id").value(1L));
    }
    
    @Test
    void getCustomer_ExistingId_ReturnsCustomer() throws Exception {
        when(customerService.getCustomerById(1L)).thenReturn(testCustomer);
        
        mockMvc.perform(get("/api/customers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }
    
    @Test
    void getCustomer_NonExistingId_ReturnsNotFound() throws Exception {
        when(customerService.getCustomerById(999L))
            .thenThrow(new RuntimeException("Customer not found"));
        
        mockMvc.perform(get("/api/customers/999"))
                .andExpect(status().isNotFound());
    }
}
```

### Running Unit Tests
```bash
# Run all unit tests
mvn test

# Run with specific profile
mvn test -P unit-test

# Generate test coverage report
mvn test jacoco:report
```

## Integration Testing

### Repository Tests
Example repository test with @DataJpaTest:

```java
package com.example.loan_origination_system.repository;

import com.example.loan_origination_system.model.people.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CustomerRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Test
    void findByIdNumber_ExistingIdNumber_ReturnsCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setFullName("Test Customer");
        customer.setIdNumber("TEST123");
        customer.setPhone("+85512345678");
        customer.setAddress("Test Address");
        
        entityManager.persist(customer);
        entityManager.flush();
        
        // When
        Customer found = customerRepository.findByIdNumber("TEST123");
        
        // Then
        assertThat(found).isNotNull();
        assertThat(found.getIdNumber()).isEqualTo("TEST123");
        assertThat(found.getFullName()).isEqualTo("Test Customer");
    }
}
```

### Service Layer Tests
Example service test with @MockBean:

```java
package com.example.loan_origination_system.service;

import com.example.loan_origination_system.model.people.Customer;
import com.example.loan_origination_system.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    
    @Mock
    private CustomerRepository customerRepository;
    
    @InjectMocks
    private CustomerService customerService;
    
    @Test
    void createCustomer_ValidCustomer_ReturnsSavedCustomer() {
        // Given
        Customer customer = new Customer();
        customer.setFullName("John Doe");
        customer.setIdNumber("ID123");
        
        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setFullName("John Doe");
        savedCustomer.setIdNumber("ID123");
        
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);
        
        // When
        Customer result = customerService.createCustomer(customer);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFullName()).isEqualTo("John Doe");
        verify(customerRepository, times(1)).save(customer);
    }
}
```

### End-to-End Tests
Example end-to-end test with @SpringBootTest:

```java
package com.example.loan_origination_system.controller;

import com.example.loan_origination_system.dto.CustomerRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CustomerControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void createAndRetrieveCustomer_IntegrationTest() throws Exception {
        // Create customer
        CustomerRequest request = new CustomerRequest();
        request.setFullName("Integration Test");
        request.setPhone("+85511111111");
        request.setIdNumber("INTEG123");
        request.setAddress("Integration Address");
        
        String requestJson = """
            {
                "fullName": "Integration Test",
                "phone": "+85511111111",
                "idNumber": "INTEG123",
                "address": "Integration Address"
            }
            """;
        
        // Create customer
        String response = mockMvc.perform(post("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();
        
        // Extract ID from response (in real test, parse JSON)
        // Then retrieve the customer
        mockMvc.perform(get("/api/customers/by-id-number/INTEG123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.fullName").value("Integration Test"));
    }
}
```

## Test Data Setup

### Using @Sql Annotation
```java
@Test
@Sql(scripts = "/test-data/customers.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/test-data/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
void getCustomers_WithTestData_ReturnsCustomers() {
    // Test with pre-loaded data
}
```

### Test Data SQL File (`src/test/resources/test-data/customers.sql`)
```sql
INSERT INTO customers (id, full_name, phone, id_number, address, status, created_at, updated_at)
VALUES 
(1, 'Test Customer 1', '+85511111111', 'ID001', 'Address 1', 'ACTIVE', NOW(), NOW()),
(2, 'Test Customer 2', '+85522222222', 'ID002', 'Address 2', 'ACTIVE', NOW(), NOW()),
(3, 'Test Customer 3', '+85533333333', 'ID003', 'Address 3', 'INACTIVE', NOW(), NOW());
```

### Using TestEntityManager
```java
@Autowired
private TestEntityManager entityManager;

private Customer createTestCustomer() {
    Customer customer = new Customer();
    customer.setFullName("Test Customer");
    customer.setPhone("+85512345678");
    customer.setIdNumber("TEST123");
    customer.setAddress("Test Address");
    return entityManager.persist(customer);
}
```

## Common Testing Scenarios

### 1. Validation Testing
Test that validation constraints are enforced:

```java
@Test
void createCustomer_EmptyName_ReturnsBadRequest() throws Exception {
    CustomerRequest request = new CustomerRequest();
    request.setFullName("");  // Empty name
    request.setPhone("+85512345678");
    request.setIdNumber("ID123");
    request.setAddress("Address");
    
    mockMvc.perform(post("/api/customers")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
}
```

### 2. Pagination Testing
```java
@Test
void getAllCustomers_WithPagination_ReturnsPage() throws Exception {
    // Create multiple test customers
    for (int i = 1; i <= 15; i++) {
        Customer customer = new Customer();
        customer.setFullName("Customer " + i);
        customer.setIdNumber("ID" + i);
        customerRepository.save(customer);
    }
    
    mockMvc.perform(get("/api/customers?page=0&size=5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.content").isArray())
            .andExpect(jsonPath("$.data.content.length()").value(5))
            .andExpect(jsonPath("$.data.totalPages").value(3));
}
```

### 3. Error Handling Testing
```java
@Test
void getCustomer_NonExistentId_ReturnsNotFound() throws Exception {
    when(customerService.getCustomerById(999L))
        .thenThrow(new RuntimeException("Customer not found with id: 999"));
    
    mockMvc.perform(get("/api/customers/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Customer not found with id: 999"));
}
```

### 4. Business Logic Testing
```java
@Test
void hasActiveLoans_CustomerWithActiveLoans_ReturnsTrue() {
    // Given: Customer with active loans
    Customer customer = createTestCustomer();
    when(customerService.hasActiveLoans(1L)).thenReturn(true);
    
    // When/Then
    mockMvc.perform(get("/api/customers/1/has-active-loans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data").value(true));
}
```

## Troubleshooting

### Common Issues and Solutions

#### 1. Tests Fail with Database Connection Errors
**Problem**: Tests fail with "Cannot get connection" errors
**Solution**: 
- Ensure H2 dependency is in pom.xml
- Check `application-test-h2.properties` configuration
- Use `@DataJpaTest` for repository tests (auto-configures H2)

#### 2. MockMvc Returns 404 for Valid Endpoints
**Problem**: Controller tests return 404 even with correct mappings
**Solution**:
- Ensure `@WebMvcTest` includes the controller class
- Check that `@RequestMapping` paths are correct
- Verify that the test is using the same context as the application

#### 3. Transaction Rollback Issues
**Problem**: Test data persists between tests
**Solution**:
- Add `@Transactional` to test class
- Use `@Sql` with cleanup scripts
- Configure H2 to use `create-drop` strategy

#### 4. JSON Serialization/Deserialization Errors
**Problem**: Tests fail with JSON parsing errors
**Solution**:
- Ensure Jackson dependencies are included
- Use `ObjectMapper` consistently
- Check date formats in request/response

#### 5. Service Layer Mocking Issues
**Problem**: `@MockBean` not working as expected
**Solution**:
- Verify `@WebMvcTest` is used for controller tests
- Use `@ExtendWith(MockitoExtension.class)` for service tests
- Ensure mocks are properly configured in `@BeforeEach`

### Debugging Tips

1. **Enable SQL Logging**:
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

2. **View H2 Console During Tests**:
   - Tests run with H2 console enabled at `/h2-console`
   - JDBC URL: `jdbc:h2:mem:testdb`
   - Username: `sa`, Password: (empty)

3. **Increase Test Timeout**:
   ```java
   @Test
   @Timeout(value = 5, unit = TimeUnit.SECONDS)
   void testWithTimeout() {
       // test code
   }
   ```

4. **Use Test Containers for Integration Tests**:
   ```java
   @Testcontainers
   @SpringBootTest
   class IntegrationTest {
       @Container
       static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
   }
   ```

## Best Practices

### 1. Test Organization
- Keep unit tests fast and isolated
- Use integration tests for end-to-end scenarios
- Separate test data setup from test logic

### 2. Test Naming Conventions
- Use descriptive test names: `methodName_scenario_expectedResult`
- Follow Given-When-Then pattern in test structure
- Include assertions for both success and failure cases

### 3. Test Data Management
- Use `@BeforeEach` for common setup
- Clean up test data after each test
- Consider using test data builders

### 4. Coverage Goals
- Aim for 80%+ line coverage
- Focus on business logic coverage over boilerplate
- Include edge cases and error scenarios

### 5. Continuous Integration
- Run tests automatically on CI/CD pipeline
- Fail build on test failures
- Generate and publish test reports

## Additional Resources

- [Spring Boot Testing Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://site.mockito.org/)
- [Testcontainers](https://www.testcontainers.org/)
- [Postman Testing Documentation](https://learning.postman.com/docs/writing-scripts/test-scripts/)

---

*Last Updated: February 2024*  
*Tested with: Spring Boot 4.0.1, Java 21, Maven 3.9+*