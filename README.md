# Tickon Microservices

A microservices-based ticketing system built with Spring Boot and Spring Cloud.

## Project Structure

```
tickon-api/
├── common/                    # Shared models and utilities
├── services/
│   ├── eureka-server/        # Service discovery server
│   ├── api-gateway/          # API Gateway (port 8080)
│   ├── event-service/        # Event management service (port 8081)
│   └── identity-service/         # Identity management service (port 8082)
├── docker-compose.yml        # Docker compose for local development
├── prometheus.yml            # Prometheus configuration
└── pom.xml                   # Parent POM
```

## Services

- **Eureka Server** (8761): Service discovery
- **API Gateway** (8080): Single entry point for all microservices
- **Event Service** (8081): Manages events and ticketing
- **Identity Service** (8082): Identity authentication and management

## Getting Started

### Prerequisites
- Java 21
- Maven 3.8+
- Docker & Docker Compose

### Building the Project

```bash
# Build all modules
mvn clean package

# Build with Docker
docker-compose build
```

### Running Locally

```bash
# Start all services
docker-compose up

# Or run individual services
mvn spring-boot:run -pl services/eureka-server
mvn spring-boot:run -pl services/api-gateway
mvn spring-boot:run -pl services/event-service
mvn spring-boot:run -pl services/identity-service
```

### Accessing Services

- Eureka Dashboard: http://localhost:8761
- API Gateway: http://localhost:8080
- Event Service: http://localhost:8081
- Identity Service: http://localhost:8082
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin)

### API Endpoints (via Gateway)

- Events: `http://localhost:8080/api/events`
- Identity: `http://localhost:8080/api/identity`

## Development

Each service can be developed independently. The common module contains shared models and utilities.

### Adding a New Service

1. Create a new module under `services/`
2. Add the module to the parent POM
3. Configure the service with Eureka client
4. Add routing rules in the API Gateway
5. Update docker-compose.yml

## Monitoring

- Actuator endpoints exposed for health checks
- Prometheus metrics available at `/actuator/prometheus`
- Grafana dashboards for visualization
