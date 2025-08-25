# Testing Strategy for Tickon Microservices

## Test Organization

```
tickon-api/
├── services/
│   ├── event-service/src/test/java/
│   │   ├── unit/              # Unit tests (isolated, mocked dependencies)
│   │   │   ├── controller/    # Controller layer tests
│   │   │   ├── service/       # Service layer tests
│   │   │   └── repository/    # Repository tests
│   │   └── integration/       # Integration tests (with real dependencies)
│   │       ├── api/           # API integration tests
│   │       └── db/            # Database integration tests
│   └── user-service/src/test/  # Same structure as event-service
│
├── tests/                      # Cross-service test modules
│   ├── e2e/                   # End-to-end tests
│   ├── contract/              # Contract tests (consumer-driven)
│   └── performance/           # Performance/load tests
│
└── common/src/test/           # Tests for shared components
```

## Test Types

### 1. Unit Tests (in each service)
- Location: `services/{service-name}/src/test/java/.../unit/`
- Fast, isolated tests with mocked dependencies
- Use `@WebMvcTest`, `@DataJpaTest`, `@MockBean`
- Naming: `*Test.java`

### 2. Integration Tests (in each service)
- Location: `services/{service-name}/src/test/java/.../integration/`
- Test with real dependencies (DB, external services)
- Use `@SpringBootTest`, `@AutoConfigureMockMvc`
- Use Testcontainers for databases
- Naming: `*IntegrationTest.java`

### 3. Contract Tests (separate module)
- Location: `tests/contract/`
- Ensure services maintain their API contracts
- Use Spring Cloud Contract or Pact
- Run during CI/CD pipeline

### 4. End-to-End Tests (separate module)
- Location: `tests/e2e/`
- Test complete user scenarios across services
- Use REST Assured or WebTestClient
- Run against docker-compose environment

### 5. Performance Tests (separate module)
- Location: `tests/performance/`
- Load and stress testing
- Use JMeter or Gatling

## Best Practices

### Package Structure Example

```
services/event-service/src/test/java/com/example/tickon/eventservice/
├── unit/
│   ├── controller/
│   │   └── EventControllerTest.java
│   ├── service/
│   │   └── EventServiceTest.java
│   └── repository/
│       └── EventRepositoryTest.java
├── integration/
│   ├── api/
│   │   └── EventApiIntegrationTest.java
│   └── db/
│       └── EventDbIntegrationTest.java
└── fixtures/                  # Test data builders
    └── EventFixture.java
```

### Test Configuration

1. **Application Properties for Tests**
   - `src/test/resources/application-test.yml`
   - Use in-memory databases for unit tests
   - Use Testcontainers for integration tests

2. **Base Test Classes**
   - Create abstract base classes for common setup
   - Example: `BaseIntegrationTest`, `BaseControllerTest`

3. **Test Data Builders**
   - Use Builder pattern for test objects
   - Keep in `fixtures` or `testdata` packages

## Maven Configuration

### Running Different Test Types

```bash
# Unit tests only
mvn test -Dtest="**/*Test"

# Integration tests only
mvn test -Dtest="**/*IntegrationTest"

# All tests in a specific service
mvn test -pl services/event-service

# E2E tests
mvn test -pl tests/e2e

# Skip tests during build
mvn clean install -DskipTests
```

### Test Profiles

Configure Maven profiles in parent POM:
- `unit-tests` - Run only unit tests
- `integration-tests` - Run integration tests
- `all-tests` - Run all tests
- `contract-tests` - Run contract tests
- `e2e-tests` - Run end-to-end tests

## CI/CD Integration

1. **Fast Feedback Loop**
   - Unit tests run on every commit
   - Integration tests run on PR creation
   - E2E tests run before deployment

2. **Parallel Execution**
   - Run service tests in parallel
   - Use Maven Surefire's parallel execution

3. **Test Reports**
   - Generate test reports with Surefire
   - Code coverage with JaCoCo
   - Publish to SonarQube

## Testing Tools Stack

- **JUnit 5** - Test framework
- **Mockito** - Mocking framework
- **AssertJ** - Fluent assertions
- **Testcontainers** - Integration testing with containers
- **REST Assured** - API testing
- **WireMock** - Mock external services
- **Spring Cloud Contract** - Contract testing
- **Gatling/JMeter** - Performance testing