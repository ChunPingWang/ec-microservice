# Customer Service Test Suite

This document describes the comprehensive test suite implemented for the Customer Service microservice.

## Test Structure

### Unit Tests (Domain Layer)
- **CustomerTest.java** - Tests Customer domain entity business logic
  - Customer creation with validation
  - Personal information updates
  - Status management (activate/deactivate)
  - Address management operations
  - Login recording
  - Equality and hash code behavior

- **AddressTest.java** - Tests Address value object validation
  - Taiwan/Taipei address validation
  - Postal code and district validation
  - Address formatting for different countries
  - Business logic for address updates
  - Validation error handling

- **CustomerRegisteredEventTest.java** - Tests domain events
  - Event creation and structure
  - Unique event ID generation
  - Timestamp validation

### Unit Tests (Application Layer)
- **CustomerServiceTest.java** - Tests application service orchestration
  - Customer registration flow
  - Customer retrieval operations
  - Customer updates with domain service integration
  - Status management operations
  - Search functionality
  - Error handling for non-existent customers

- **AddressManagementServiceTest.java** - Tests address management use cases
  - Adding addresses to customers
  - Updating existing addresses
  - Removing addresses with business rules
  - Setting primary addresses

### Integration Tests (Infrastructure Layer)
- **CustomerJpaAdapterIntegrationTest.java** - Tests database operations
  - Customer persistence and retrieval
  - Address relationship management
  - Query operations (by email, name, city)
  - Status updates persistence
  - Complex address operations

### API Integration Tests
- **CustomerControllerIntegrationTest.java** - Tests REST API endpoints
  - Customer registration API
  - Customer retrieval endpoints
  - Customer update operations
  - Status management endpoints
  - Search functionality
  - Error handling and validation
  - HTTP status codes and response formats

## Test Coverage

### Domain Logic Coverage
✅ Customer creation and validation  
✅ Address validation (Taiwan/Taipei specific)  
✅ Business rules enforcement  
✅ Domain events  
✅ Status management  
✅ Address management operations  

### Application Layer Coverage
✅ Use case orchestration  
✅ Domain service integration  
✅ Event publishing  
✅ Error handling  
✅ Transaction boundaries  

### Infrastructure Coverage
✅ Database operations  
✅ JPA entity mapping  
✅ Query operations  
✅ Relationship management  
✅ REST API endpoints  
✅ Request/response handling  
✅ Validation integration  

## Test Configuration

### Test Database
- Uses H2 in-memory database for fast test execution
- Database schema created/dropped for each test
- Configured in `application-test.yml`

### Test Profiles
- `@ActiveProfiles("test")` for integration tests
- Separate configuration for test environment
- Reduced logging for cleaner test output

### Test Dependencies
- JUnit 5 for test framework
- Mockito for mocking
- Spring Boot Test for integration testing
- RestAssured for API testing
- H2 Database for test persistence

## Running Tests

### Individual Test Classes
```bash
./gradlew :customer-service:test --tests CustomerTest
./gradlew :customer-service:test --tests CustomerServiceTest
```

### All Tests
```bash
./gradlew :customer-service:test
```

### Test Suite
```bash
./gradlew :customer-service:test --tests CustomerServiceTestSuite
```

## Test Requirements Mapping

### Requirement 8.1 - Unit Tests for Domain Logic
✅ CustomerTest - Tests all customer business rules  
✅ AddressTest - Tests address validation and Taiwan-specific rules  
✅ CustomerRegisteredEventTest - Tests domain events  

### Requirement 8.4 - Integration Tests for Database Operations
✅ CustomerJpaAdapterIntegrationTest - Tests all persistence operations  
✅ Complex query testing (by city, name, status)  
✅ Relationship management testing  

### API Testing for REST Endpoints
✅ CustomerControllerIntegrationTest - Tests all REST endpoints  
✅ Request/response validation  
✅ Error handling and status codes  
✅ End-to-end API flow testing  

## Test Quality Metrics

- **Coverage**: Tests cover all public methods and business logic
- **Isolation**: Each test is independent and can run in any order
- **Performance**: Fast execution with in-memory database
- **Maintainability**: Clear test names and structure
- **Reliability**: Deterministic tests with proper setup/teardown