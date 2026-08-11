# BankHub

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen?logo=springboot)
![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.0-blue?logo=spring)
![Gradle](https://img.shields.io/badge/Gradle-8.x-02303A?logo=gradle)
![License](https://img.shields.io/badge/License-Educational-lightgrey)

Enterprise-grade banking platform built with microservices architecture, implementing Domain-Driven Design, Event-Driven Architecture, and Hexagonal Architecture patterns.

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Authentication & Authorization](#authentication--authorization)
- [API Integration](#api-integration)
- [Security Validation](#security-validation)
- [Observability & Monitoring](#observability--monitoring)
- [Development Guide](#development-guide)
- [Configuration Reference](#configuration-reference)
- [Build & Deployment](#build--deployment)
- [Troubleshooting](#troubleshooting)
- [Additional Documentation](#additional-documentation)
- [Recent Updates](#recent-updates)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

BankHub is a comprehensive banking platform implementing modern microservices architecture with enterprise-grade patterns and practices. The system supports complete banking operations including account management, investment portfolios, PIX transactions, customer onboarding workflows, and AI-powered financial assistance.

**Key Capabilities:**
- Account lifecycle management with KYC verification
- Investment portfolio management (stocks, ETFs, fixed income)
- PIX payment processing with distributed ledger
- Automated customer onboarding with risk analysis
- AI-powered financial assistant using LLMs
- Real-time notifications via email/SMS
- Distributed tracing and observability

**Architecture Highlights:**
- 7 microservices following Hexagonal Architecture (Ports & Adapters)
- Domain-Driven Design with bounded contexts
- Event-driven communication via Apache Kafka
- OAuth2/JWT authentication with Keycloak
- Distributed tracing with OpenTelemetry and Jaeger
- Workflow orchestration using Camunda Zeebe

---

## Architecture

### Microservices Overview

| Service | Port | Purpose | Technology Stack |
|---------|------|---------|------------------|
| **API Gateway** | 8080 | Request routing, rate limiting, authentication, CORS | Spring Cloud Gateway, Redis |
| **Account Service** | 8081 | Account management, cards, KYC, selfie verification | Spring Boot, MongoDB, Kafka, Redis |
| **Onboarding Service** | 8082 | Customer onboarding, risk analysis workflows | Spring Boot, Camunda Zeebe, Kafka |
| **Notification Service** | 8083 | Email/SMS notifications, event-driven alerts | Spring Boot, PostgreSQL, Kafka, Spring Mail |
| **Hub IA Service** | 8084 | AI financial assistant, natural language queries | Spring Boot, LangChain4j, Redis, LiteLLM |
| **Transaction Service** | 8085 | PIX transactions, ledger, account statements | Spring Boot, MongoDB, Kafka |
| **Investment Service** | 8086 | Portfolio management, trading, home broker | Spring Boot, MongoDB, Kafka |

### Infrastructure Components

| Component | Port(s) | Purpose | Version |
|-----------|---------|---------|---------|
| **MongoDB** | 27017 | Primary data store for Account, Transaction, Investment services | 7.0 |
| **PostgreSQL** | 5432 | Relational storage for Notification Service | 15 |
| **Redis** | 6379 | Caching, rate limiting, session management | 7.2 |
| **Apache Kafka** | 9092 | Event streaming, asynchronous messaging | 7.4.4 |
| **Zookeeper** | 2181 | Kafka cluster coordination | 7.4.4 |
| **Keycloak** | 9000 | OAuth2/JWT authentication, user management | 23.0.4 |
| **Jaeger** | 16686 | Distributed tracing UI | 1.52 |
| **OpenTelemetry Collector** | 4317, 4318 | Trace and metrics collection | 0.91.0 |
| **Camunda Zeebe** | 26500 | Workflow engine for onboarding processes | 8.4.0 |
| **Elasticsearch** | 9200 | Zeebe data storage, search | 8.12.0 |
| **Operate** | 8081 | Zeebe workflow monitoring UI | 8.4.0 |
| **LiteLLM** | 4000 | LLM proxy supporting multiple providers | Latest |
| **MailHog** | 1025, 8025 | Email testing (development) | 1.0.1 |
| **Postfix** | 1587 | SMTP server (production) | 3.8.0 |

### Communication Patterns

**Synchronous (HTTP via OpenFeign):**
- Transaction Service → Account Service (account debit/validation)
- Investment Service → Account Service (fund transfers)
- Hub IA Service → Account Service (contextual data retrieval)
- All external requests → API Gateway → Target Service

**Asynchronous (Kafka Event Bus):**
- Account Service publishes: `AccountCreatedEvent`, `AccountStatusChangedEvent`, `PixProcessedEvent`
- Transaction Service publishes: `TransactionEventMessage`, `SagaReplyMessage`
- Notification Service consumes all service events for alert delivery
- Saga pattern implementation for distributed transactions (choreography-based)

### Architecture Patterns

- **Hexagonal Architecture:** All services implement Ports & Adapters pattern with clear separation of domain, application, and infrastructure layers
- **Domain-Driven Design:** Bounded contexts per service, aggregates as domain roots, value objects, domain events
- **Event-Driven Architecture:** Kafka-based event streaming for loose coupling and eventual consistency
- **CQRS (Implicit):** Write to MongoDB, read from Redis cache for performance optimization
- **API Gateway Pattern:** Centralized routing with rate limiting, authentication, and custom filters
- **Circuit Breaker:** Resilience4j implementation for external service calls

---

## Technology Stack

### Build & Runtime
- **Java:** 21 (Eclipse Temurin)
- **Build Tool:** Gradle 8.x
- **Spring Boot:** 3.2.2
- **Spring Cloud:** 2023.0.0

### Frameworks & Libraries
- **Spring Cloud Gateway:** API Gateway implementation
- **Spring Security:** OAuth2 Resource Server
- **Spring Data:** MongoDB, Redis, JPA repositories
- **Spring Kafka:** Event streaming integration
- **LangChain4j:** 0.27.1 (LLM integration framework)
- **MapStruct:** 1.5.5 (DTO mapping)
- **Resilience4j:** Circuit breaker, retry patterns

### Persistence
- **MongoDB:** 7.0 (Document store for Account, Transaction, Investment)
- **PostgreSQL:** 15 (Relational database for Notification Service)
- **Redis:** 7.2 (Caching, rate limiting, sessions)

### Messaging & Events
- **Apache Kafka:** 7.4.4 (Event streaming platform)
- **Zookeeper:** 7.4.4 (Kafka coordination)

### Authentication & Security
- **Keycloak:** 23.0.4 (OAuth2/JWT provider)
- **Spring Security OAuth2:** Resource server validation

### Observability
- **OpenTelemetry:** Distributed tracing instrumentation
- **Jaeger:** 1.52 (Trace visualization)
- **OTLP Collector:** 0.91.0 (Telemetry collection)
- **Prometheus:** Metrics exposition
- **Micrometer:** Metrics instrumentation
- **Spring Boot Actuator:** Health checks and endpoints

### Workflow & Orchestration
- **Camunda Zeebe:** 8.4.0 (Workflow engine)
- **Operate:** 8.4.0 (Workflow monitoring UI)
- **Elasticsearch:** 8.12.0 (Zeebe data store)

### AI & LLM
- **LangChain4j:** 0.27.1 (LLM framework)
- **LiteLLM:** Multi-provider LLM proxy
- **Ollama:** Local LLM runtime (gemma2 model)

### Email & Notifications
- **MailHog:** 1.0.1 (SMTP testing for development)
- **Postfix:** 3.8.0 (Production SMTP server)
- **Spring Mail:** Email integration

### Testing
- **JUnit 5:** Test framework
- **Mockito:** Mocking framework
- **AssertJ:** Fluent assertions
- **TestContainers:** Integration testing with containers
- **REST Assured:** 5.4.0 (API testing)
- **GreenMail:** Email testing

### Containerization
- **Docker:** Container runtime
- **Docker Compose:** Multi-container orchestration
- **Alpine Linux:** Base image for minimal container size

---

## Prerequisites

Before starting, ensure you have the following installed:

- **Docker:** 20.10 or higher
- **Docker Compose:** 2.0 or higher
- **Java Development Kit:** 21 (for local development)
- **Gradle:** 8.x (included via wrapper)

**System Requirements:**
- Minimum 8GB RAM (16GB recommended for running all services)
- 20GB free disk space
- Linux, macOS, or Windows with WSL2

**Verification:**

```bash
docker --version
docker-compose --version
java -version  # Java 21
```

---

## Getting Started

### 1. Infrastructure Setup

Start all infrastructure components (databases, message brokers, auth, observability):

```bash
docker-compose -f docker-compose-infra.yml up -d
```

Verify all containers are running:

```bash
docker-compose -f docker-compose-infra.yml ps
```

**Important:** Wait approximately 30 seconds for Keycloak, Kafka, and MongoDB to fully initialize before proceeding.

### 2. Application Build

Build all microservices:

```bash
./gradlew clean build -x test
```

### 3. Application Deployment

**Option A: Docker Deployment (Recommended)**

```bash
docker-compose -f docker-compose-apps.yml up -d
```

**Option B: Local Development**

Run services individually for development:

```bash
./gradlew :account-service:bootRun
./gradlew :investment-service:bootRun
./gradlew :transaction-service:bootRun
# ... other services as needed
```

### 4. Verification

Verify all services are healthy:

```bash
# API Gateway
curl http://localhost:8080/actuator/health

# Account Service
curl http://localhost:8081/actuator/health

# Investment Service
curl http://localhost:8086/actuator/health

# Transaction Service
curl http://localhost:8085/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

---

## Authentication & Authorization

BankHub uses Keycloak for centralized OAuth2/JWT authentication across all microservices.

### Keycloak Admin Console

Access the Keycloak administration console:

- **URL:** http://localhost:9000
- **Username:** `admin`
- **Password:** `admin`
- **Realm:** `bankhub`

### Pre-configured Test Users

The `bankhub` realm includes 3 test users:

| Email | Senha | Role | Customer ID |
|-------|-------|------|-------------|
| `customer1@bankhub.com` | `senha123` | customer | `customer-001` |
| `customer2@bankhub.com` | `senha123` | customer | `customer-002` |
| `admin@bankhub.com` | `admin123` | admin | - |

### JWT Token Acquisition

**Using Postman/Insomnia:**

```http
POST http://localhost:9000/realms/bankhub/protocol/openid-connect/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
client_id=bankhub-services
client_secret=bankhub-secret-2024
username=customer1@bankhub.com
password=senha123
```

**Using cURL:**

```bash
# Acquire token for customer1
curl -X POST 'http://localhost:9000/realms/bankhub/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=bankhub-services' \
  -d 'client_secret=bankhub-secret-2024' \
  -d 'username=customer1@bankhub.com' \
  -d 'password=senha123'

# Extract access_token only (requires jq)
TOKEN=$(curl -s -X POST 'http://localhost:9000/realms/bankhub/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=bankhub-services' \
  -d 'client_secret=bankhub-secret-2024' \
  -d 'username=customer1@bankhub.com' \
  -d 'password=senha123' | jq -r .access_token)

echo $TOKEN
```

### JWT Token Claims

The returned token includes:
- `customerId` claim (used in `X-User-Id` header)
- `roles` claim (customer/admin)
- **Expiration:** 30 minutes
- **Issuer:** `http://keycloak:8080/realms/bankhub`

---

## API Integration

### Via API Gateway (Recommended)

All external requests must route through the API Gateway on port **8080**:

**Example Requests:**

```bash
# Use the token acquired previously
TOKEN="your_token_here"

# Create account
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "cpf": "12345678901",
    "name": "João Silva",
    "email": "joao@example.com",
    "birthdate": "1990-01-01"
  }'

# Get account details
curl http://localhost:8080/api/v1/accounts/{accountId} \
  -H "Authorization: Bearer $TOKEN"

# Deposit funds
curl -X POST http://localhost:8080/api/v1/accounts/{accountId}/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 1000.00,
    "transactionPin": "1234"
  }'

# Get investment portfolio
curl http://localhost:8080/api/v1/investments/portfolio/customer-001 \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-User-Id: customer-001"
```

**Expected Response (Create Account):**

```json
{
  "accountId": "acc_abc123",
  "customerId": "customer-001",
  "status": "PENDING_ACTIVATION",
  "balance": 0.00,
  "createdAt": "2026-08-10T10:30:00Z"
}
```

### OpenAPI Documentation

Each service exposes interactive OpenAPI/Swagger UI:

| Service | Swagger UI URL |
|---------|----------------|
| **API Gateway** | http://localhost:8080/swagger-ui.html |
| **Account Service** | http://localhost:8081/swagger-ui.html |
| **Investment Service** | http://localhost:8086/swagger-ui.html |
| **Transaction Service** | http://localhost:8085/swagger-ui.html |
| **Hub IA Service** | http://localhost:8084/swagger-ui.html |

---

## Security Validation

### Portfolio Authorization Test

The Investment Service enforces authorization to ensure users can only access their own portfolios.

**Test Case 1: Authorized Access**

```bash
# customer-001 accessing own portfolio (should return 200 OK)
curl -i http://localhost:8080/api/v1/investments/portfolio/customer-001 \
  -H "Authorization: Bearer $TOKEN_CUSTOMER1" \
  -H "X-User-Id: customer-001"
```

**Expected Response:**
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "customerId": "customer-001",
  "totalValue": 15000.00,
  "positions": [...]
}
```

**Test Case 2: Unauthorized Access**

```bash
# customer-001 attempting to access customer-002's portfolio (should return 403 Forbidden)
curl -i http://localhost:8080/api/v1/investments/portfolio/customer-002 \
  -H "Authorization: Bearer $TOKEN_CUSTOMER1" \
  -H "X-User-Id: customer-001"
```

**Expected Response:**
```
HTTP/1.1 403 Forbidden
Content-Type: application/json

{
  "error": "Forbidden",
  "message": "Você não tem permissão para acessar a carteira de outro cliente."
}
```

---

## Observability & Monitoring

### Distributed Tracing with Jaeger

**Jaeger UI:** http://localhost:16686

Visualize distributed traces across microservices:

1. Access Jaeger UI at http://localhost:16686
2. Select a service (e.g., `account-service`, `investment-service`)
3. Click "Find Traces" to view recent requests
4. Explore individual spans to analyze latency and service dependencies

**Trace Context Propagation:**
- All services automatically propagate trace context via OpenTelemetry
- Traces include correlation IDs for debugging distributed transactions
- Sampling rate: 100% (configurable for production)

### Metrics Collection

**Prometheus Exporter:** http://localhost:8888/metrics

Access raw metrics in Prometheus format for monitoring:
- JVM metrics (heap, threads, GC)
- HTTP request metrics (count, duration, errors)
- Custom business metrics (account creations, transactions)

### Email Testing (Development)

**MailHog UI:** http://localhost:8025

Preview emails sent during development:
- Account activation emails
- Transaction notifications
- Password reset links
- All SMTP traffic is captured locally

---

## Development Guide

### Hexagonal Architecture (Ports & Adapters)

All microservices follow consistent hexagonal architecture:

```
com.bankhub.{service}/
├── domain/                    # Pure business logic, framework-free
│   ├── {Aggregate}.java      # Domain aggregates (e.g., Account, Transaction)
│   ├── {ValueObject}.java    # Immutable value objects
│   ├── event/                # Domain events
│   └── exception/            # Domain exceptions
│
├── application/              # Use case orchestration
│   ├── port/
│   │   ├── in/              # Input ports (use case interfaces)
│   │   │   ├── CreateAccountUseCase
│   │   │   └── ProcessPixUseCase
│   │   └── out/             # Output ports (dependency interfaces)
│   │       ├── AccountPersistencePort
│   │       └── AccountMessagingPort
│   └── service/             # Use case implementations
│       ├── CreateAccountService
│       └── ProcessPixService
│
└── infrastructure/           # Framework-specific adapters
    ├── web/                 # REST controllers
    ├── persistence/         # MongoDB/JPA adapters
    ├── messaging/           # Kafka publishers/listeners
    ├── client/              # OpenFeign HTTP clients
    └── config/              # Spring configuration
```

**Key Principles:**
- Dependencies flow inward: Infrastructure → Application → Domain
- Domain layer has zero framework dependencies
- All external dependencies defined as interfaces (ports)
- Adapters implement ports in infrastructure layer

### Running Tests

```bash
# Run all tests across all services
./gradlew test

# Test specific service
./gradlew :account-service:test

# Integration tests (requires Docker for Testcontainers)
./gradlew :account-service:test --tests "*IntegrationTest"

# Test with coverage
./gradlew test jacocoTestReport
```

**Note:** Integration tests use Testcontainers to spin up MongoDB, Kafka, and PostgreSQL containers automatically.

### Code Coverage Reports

BankHub uses JaCoCo for code coverage measurement with an 80% minimum threshold (60% for API Gateway).

**Generate coverage reports:**

```bash
# Generate coverage report for specific service
./gradlew :account-service:test jacocoTestReport

# Generate coverage for all services
./gradlew test jacocoTestReport

# Verify coverage meets minimum threshold
./gradlew jacocoTestCoverageVerification

# Run tests with coverage verification (fails build if below threshold)
./gradlew check
```

**View coverage reports:**

HTML reports are generated at:
- **Account Service:** `account-service/build/reports/jacoco/test/html/index.html`
- **Transaction Service:** `transaction-service/build/reports/jacoco/test/html/index.html`
- **Investment Service:** `investment-service/build/reports/jacoco/test/html/index.html`
- **Notification Service:** `notification-service/build/reports/jacoco/test/html/index.html`
- **Onboarding Service:** `onboarding-service/build/reports/jacoco/test/html/index.html`
- **Hub IA Service:** `hub-ia-service/build/reports/jacoco/test/html/index.html`
- **API Gateway:** `api-gateway/build/reports/jacoco/test/html/index.html`

**Coverage thresholds:**
- Most services: **80% minimum** line coverage
- API Gateway: **60% minimum** line coverage (routing-only service)

**Excluded from coverage:**
- Spring Boot Application classes (`*Application.java`)
- Configuration classes (`**/config/**`)
- DTOs (`**/adapter/in/dto/**`)

### Code Standards

| Aspect | Standard | Rationale |
|--------|----------|-----------|
| **DTOs** | Java `record` types | Immutability, conciseness (Java 21+) |
| **Validation** | Jakarta Validation annotations | Declarative validation (`@NotNull`, `@NotBlank`, `@Valid`) |
| **Mapping** | MapStruct | Compile-time safe mapping, no reflection |
| **Testing** | JUnit 5 + Mockito + AssertJ | Industry standard testing stack |
| **Integration Tests** | Testcontainers | Real database/messaging integration |
| **API Docs** | SpringDoc OpenAPI 3.0 | Auto-generated from code annotations |

### Local Development

Run services locally without Docker:

```bash
# Ensure infrastructure is running
docker-compose -f docker-compose-infra.yml up -d

# Run service with hot reload
./gradlew :account-service:bootRun

# Or use your IDE to run the main application class
# e.g., AccountServiceApplication.java
```

---

## Configuration Reference

### Keycloak (Required)

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `KEYCLOAK_JWK_URI` | `http://keycloak:8080/realms/bankhub/protocol/openid-connect/certs` | JWK Set URI for JWT validation |
| `KEYCLOAK_ISSUER_URI` | `http://keycloak:8080/realms/bankhub` | Token issuer URI |

### MongoDB

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `SPRING_DATA_MONGODB_URI` | `mongodb://admin:admin@localhost:27017/bankhub_account?authSource=admin` | MongoDB connection string |
| `MONGODB_DATABASE` | `bankhub_account` | Database name (varies by service) |

### PostgreSQL (Notification Service)

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/notification_db` | PostgreSQL JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |

### Kafka

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker addresses |
| `SPRING_KAFKA_CONSUMER_GROUP_ID` | `{service}-consumer-group` | Consumer group ID |

### OpenTelemetry (Optional)

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://otel-collector:4317` | OTLP gRPC endpoint |
| `OTEL_SERVICE_NAME` | `{service-name}` | Service name in traces |
| `OTEL_TRACES_SAMPLER` | `always_on` | Trace sampling strategy |

### Email Configuration

**Development (MailHog):**

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `SPRING_MAIL_HOST` | `mailhog` | SMTP server host |
| `SPRING_MAIL_PORT` | `1025` | SMTP port |

**Production (Postfix):**

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `SPRING_MAIL_HOST` | `postfix` | SMTP server host |
| `SPRING_MAIL_PORT` | `587` | SMTP port |
| `SPRING_PROFILES_ACTIVE` | `prod` | Enable production profile |

---

## Build & Deployment

### Local Build

Build all services:

```bash
./gradlew clean build
```

Skip tests during build:

```bash
./gradlew clean build -x test
```

### Docker Image Build

**Build individual service:**

```bash
docker build -t bankhub/account-service:latest ./account-service
```

**Build all services via Docker Compose:**

```bash
docker-compose -f docker-compose-apps.yml build
```

### Docker Compose Profiles

**Development mode (MailHog for email):**

```bash
docker-compose -f docker-compose-infra.yml up -d
```

**Production mode (Postfix for email):**

```bash
docker-compose -f docker-compose-infra.yml --profile production up -d
```

### Production Considerations

- Configure persistent volumes for MongoDB and PostgreSQL
- Adjust JVM heap sizes via `JAVA_OPTS` environment variable
- Enable authentication for Redis in production
- Configure Kafka replication factor > 1 for high availability
- Use external secret management (e.g., Vault) instead of hardcoded credentials
- Set up monitoring alerts on Prometheus metrics
- Configure log aggregation (ELK/Loki)

---

## Troubleshooting

### Keycloak Issues

**Problem:** Keycloak fails to start

```bash
# Check logs
docker logs bank-keycloak

# Restart Keycloak
docker-compose -f docker-compose-infra.yml restart keycloak

# Verify realm import
docker exec bank-keycloak ls /opt/keycloak/data/import
```

### Authentication Issues

**Problem:** JWT token invalid (401 Unauthorized)

- Verify realm is `bankhub` (not `master`)
- Check token expiration (tokens expire after 30 minutes)
- Ensure `Authorization: Bearer {token}` header is present
- Verify `X-User-Id` header matches token's `customerId` claim

**Problem:** Token expired

```bash
# Acquire new token
curl -X POST 'http://localhost:9000/realms/bankhub/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'grant_type=password' \
  -d 'client_id=bankhub-services' \
  -d 'client_secret=bankhub-secret-2024' \
  -d 'username=customer1@bankhub.com' \
  -d 'password=senha123'
```

### Database Connection Issues

**Problem:** MongoDB connection refused

```bash
# Verify MongoDB is running
docker ps | grep mongo

# Check MongoDB logs
docker logs bank-mongodb

# Test connection
docker exec -it bank-mongodb mongosh -u admin -p admin --authenticationDatabase admin
```

**Problem:** PostgreSQL connection refused

```bash
# Verify PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs bank-postgres
```

### Kafka Issues

**Problem:** Kafka consumer not processing messages

```bash
# List topics
docker exec -it bank-kafka kafka-topics --list --bootstrap-server localhost:9092

# View messages in topic
docker exec -it bank-kafka kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic bankhub.account.events \
  --from-beginning

# Check consumer group lag
docker exec -it bank-kafka kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --describe \
  --group account-service-consumer-group
```

### Service Health Issues

**Problem:** Service returns 503 Service Unavailable

```bash
# Check health endpoint
curl http://localhost:8081/actuator/health

# Check detailed health with components
curl http://localhost:8081/actuator/health/readiness
curl http://localhost:8081/actuator/health/liveness
```

---

## Additional Documentation

### Internal Documentation

- [API Integration Guide](docs/API_INTEGRATION_GUIDE.md) - Detailed API integration examples

### External Resources

- **Spring Boot Documentation:** https://docs.spring.io/spring-boot/docs/3.2.2/reference/html/
- **Spring Cloud Gateway:** https://spring.io/projects/spring-cloud-gateway
- **Keycloak Documentation:** https://www.keycloak.org/documentation
- **OpenTelemetry Java:** https://opentelemetry.io/docs/instrumentation/java/
- **Jaeger Documentation:** https://www.jaegertracing.io/docs/
- **Testcontainers:** https://www.testcontainers.org/
- **Camunda Platform 8:** https://docs.camunda.io/
- **Apache Kafka:** https://kafka.apache.org/documentation/
- **LangChain4j:** https://docs.langchain4j.dev/

### API Documentation

Each service exposes OpenAPI 3.0 documentation accessible via Swagger UI (see [API Integration](#api-integration) section).

---

## Recent Updates

**2026-08-06 - Security Enhancement**
- Fixed authorization vulnerability in Investment Service
- Portfolio endpoint now validates user ownership before returning data
- Added 403 Forbidden response for unauthorized access attempts

**2026-08-05 - Keycloak Integration**
- Integrated Keycloak 23.0.4 for centralized authentication
- Pre-configured `bankhub` realm with test users
- JWT tokens include `customerId` claim for authorization
- All services validate JWT via JWK Set URI

**2026-08-04 - Observability Stack**
- Added OpenTelemetry Collector and Jaeger for distributed tracing
- Configured automatic trace propagation across all services
- Exposed Prometheus metrics endpoints on all services

**2026-08-03 - Email Infrastructure**
- Configured MailHog for development email testing
- Added Postfix for production email delivery
- Implemented Spring profiles for environment-specific email configuration

**2026-08-02 - Test Infrastructure**
- Configured Testcontainers for integration testing
- Added base test classes for MongoDB, Kafka, and PostgreSQL tests
- Set up test coverage reporting with JaCoCo

---

## Contributing

We welcome contributions to BankHub! Please follow these guidelines:

### Branch Naming

- Features: `feature/description`
- Bug fixes: `bugfix/description`
- Hotfixes: `hotfix/description`

### Commit Convention

Follow Conventional Commits specification:

- `feat:` New feature
- `fix:` Bug fix
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `docs:` Documentation changes
- `chore:` Maintenance tasks

**Example:**
```bash
git commit -m "feat: add portfolio rebalancing endpoint"
```

### Pull Request Process

1. Create a feature branch from `main`
2. Follow code standards (hexagonal architecture, DTOs as records)
3. Add unit and integration tests (minimum 80% coverage)
4. Update documentation if adding new features
5. Ensure all tests pass: `./gradlew test`
6. Submit pull request with clear description

### Code Review Criteria

- Hexagonal architecture principles followed
- Comprehensive test coverage
- No security vulnerabilities introduced
- OpenAPI documentation updated
- No breaking changes without migration path

---

## License

This project is for educational and learning purposes. Free to use and modify.

---

**Version:** 1.0.0-SNAPSHOT  
**Last Updated:** August 2026
