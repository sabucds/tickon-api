# Identity Service — Observability & Monitoring Plan

- **Status:** READY FOR IMPLEMENTATION
- **Date:** 2026-02-18
- **Service:** `identity-service` (foundations portable to all services)
- **Branch:** `feat/monitoring`

---

## Goal

Build production-grade observability for the identity-service covering the three pillars:
**Logs → Metrics → Traces**, then surface everything in Grafana.

Current state: Prometheus scraping is wired up at the infra level, but the service emits only default Spring/JVM metrics and unstructured plaintext logs. There is no tracing, no business-level metrics, and no log aggregation.

---

## Observability Architecture

```
identity-service
  │
  ├─ Structured JSON Logs ──► Promtail ──► Loki ──► Grafana (LogQL)
  ├─ Micrometer Metrics ────► Prometheus ──────────► Grafana (PromQL)
  └─ Micrometer Tracing ───► Tempo ────────────────► Grafana (TraceQL)
                                                        │
                                              Grafana exemplars link
                                              metrics ↔ traces ↔ logs
```

The three backends (Loki, Tempo, Prometheus) are correlated in Grafana via
`traceId` / `spanId`, which appear in both log fields and trace spans.

---

## Scope

### In scope
- Structured JSON logging with `traceId`, `spanId`, `userId` MDC fields
- Distributed tracing via **Micrometer Tracing + OpenTelemetry + Grafana Tempo**
- Custom business metrics (Micrometer counters/timers) for all user and auth operations
- `IdentityMdcFilter` — HTTP filter injecting correlation IDs into MDC per request
- Log aggregation stack: **Loki + Promtail** added to docker-compose
- Tempo added to docker-compose
- Three Grafana dashboards (provisioned as JSON)
- Prometheus alerting rules file
- Flyway migration for a `metrics_audit_log` table (optional structured DB audit)

### Out of scope
- Changes to `event-service`, `api-gateway`, or `eureka-server` (foundations are portable; propagation is a follow-up)
- Grafana alerts (alertmanager wiring) — covered in alert rules file; routing is a follow-up
- Production log rotation or secret management

---

## Tech Decisions

| Concern | Choice | Rationale |
|---|---|---|
| Tracing backend | Grafana Tempo | Native Grafana integration, zero-dependency trace storage, free |
| Trace instrumentation | Micrometer Tracing + OTel bridge | Spring Boot 3 native; avoids Zipkin legacy |
| Log format | Logback JSON (logstash-logback-encoder) | Machine-parseable, Loki-compatible |
| Log shipping | Promtail | Lightweight; reads Docker log files; native Loki labels |
| Log aggregation | Grafana Loki | Low-cost indexing; pairs perfectly with Promtail + Grafana |
| Metrics | Micrometer (already wired) | Already present; add custom meters only |

---

## Phase 1 — Structured Logging

### 1.1 Dependency

Add to `services/identity-service/pom.xml`:
```xml
<dependency>
  <groupId>net.logstash.logback</groupId>
  <artifactId>logstash-logback-encoder</artifactId>
  <version>7.4</version>
</dependency>
```

### 1.2 Logback Configuration

Create `src/main/resources/logback-spring.xml`:

```xml
<configuration>
  <springProfile name="!local">
    <!-- JSON output for deployed/docker environments -->
    <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
      <encoder class="net.logstash.logback.encoder.LogstashEncoder">
        <includeMdcKeyName>traceId</includeMdcKeyName>
        <includeMdcKeyName>spanId</includeMdcKeyName>
        <includeMdcKeyName>userId</includeMdcKeyName>
        <includeMdcKeyName>operation</includeMdcKeyName>
        <customFields>{"service":"identity-service"}</customFields>
      </encoder>
    </appender>
    <root level="INFO">
      <appender-ref ref="JSON_CONSOLE"/>
    </root>
  </springProfile>

  <springProfile name="local">
    <!-- Human-readable for local dev -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
      <encoder>
        <pattern>%d{HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n</pattern>
      </encoder>
    </appender>
    <root level="DEBUG">
      <appender-ref ref="CONSOLE"/>
    </root>
  </springProfile>
</configuration>
```

### 1.3 MDC Filter

Create `shared/infrastructure/web/IdentityMdcFilter.java`:

- Implements `OncePerRequestFilter` (order = `HIGHEST_PRECEDENCE + 1`)
- Reads `X-User-Id` header (set by api-gateway after JWT validation); populates `MDC.put("userId", ...)`
- `traceId` and `spanId` are automatically propagated into MDC by Micrometer Tracing (via `MDCScopeDecorator`) — no manual wiring needed once tracing is on
- Register as a `@Bean` in a `WebConfig` or via `@Component`

### 1.4 Logging in Application Services

Add structured log statements to each application service. Follow this pattern:

```java
// LoginService example
log.info("Login attempt for user '{}'", usernameOrEmail);          // before
log.info("Login successful: sessionId={}", session.getId());        // success
log.warn("Login failed: invalid credentials for '{}'", usernameOrEmail); // failure
```

Services to instrument:
- `RegisterUserService` — info on success; warn on duplicate email/username
- `GetUserByIdService` — debug on hit; warn on not found
- `DeleteUserService` — info on deletion
- `LoginService` — info on attempt/success; warn on failure with reason
- `RefreshTokenService` — debug on success; warn on expired/revoked/invalid
- `LogoutService` — info on logout
- `RequestPasswordResetService` — info on request; never log the token
- `VerifyResetTokenService` — debug
- `ResetPasswordService` — info on completion; warn on invalid/expired token
- `SendPasswordResetEmailHandler` — already has logs; review error log includes `traceId`
- `RevokeSessionsOnPasswordResetHandler` — add info log

**Log-level policy:**
| Level | When |
|---|---|
| `DEBUG` | Internal state, query resolution |
| `INFO` | Successful business operations |
| `WARN` | Expected failures (wrong credentials, expired tokens, not found) |
| `ERROR` | Unexpected failures (infrastructure errors, unhandled exceptions) |

**Security rules:**
- Never log: raw passwords, token values, password hashes, full email addresses in error paths
- Safe to log: userId (UUID), sessionId (UUID), username, operation names, error codes

---

## Phase 2 — Distributed Tracing

### 2.1 Dependencies

Add to `services/identity-service/pom.xml`:
```xml
<!-- Micrometer Tracing core -->
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-tracing-bridge-otel</artifactId>
</dependency>
<!-- OpenTelemetry exporter → Tempo via OTLP -->
<dependency>
  <groupId>io.opentelemetry</groupId>
  <artifactId>opentelemetry-exporter-otlp</artifactId>
</dependency>
```

`micrometer-tracing-bridge-otel` is managed by Spring Boot 3's BOM — no explicit version needed.

### 2.2 Configuration

Add to `application.yml`:
```yaml
management:
  tracing:
    sampling:
      probability: 1.0   # 100% in dev; tune to 0.1 in prod
  otlp:
    tracing:
      endpoint: http://tempo:4318/v1/traces

logging:
  pattern:
    correlation: "[${spring.application.name:},%X{traceId},%X{spanId}] "
```

Spring Boot auto-configures the OTel exporter, MDC propagation, and HTTP instrumentation from this config alone.

### 2.3 What Gets Traced Automatically

- Every HTTP request → span with `http.method`, `http.route`, `http.status_code`
- Every Spring Data JPA query → child span with SQL statement
- Async `@EventListener` handlers → linked spans (Micrometer Tracing propagates context)

### 2.4 Manual Spans (Optional Enhancement)

For critical business paths, add manual span enrichment:

```java
// LoginService — add outcome tag to span
@Autowired Tracer tracer;

Span span = tracer.currentSpan();
if (span != null) {
    span.tag("auth.outcome", "success");
    span.tag("auth.sessionId", sessionId.toString());
}
```

Apply to: `LoginService`, `RefreshTokenService`, `ResetPasswordService`.

---

## Phase 3 — Business Metrics

### 3.1 Metric Registry Bean

Create `shared/infrastructure/metrics/IdentityMetrics.java`:

A central registry class injected with `MeterRegistry`. Defines all custom meters as constants to avoid typos.

### 3.2 Metric Catalog

All metrics are prefixed `identity.` per Micrometer convention.

#### User Module
| Metric | Type | Tags | Description |
|---|---|---|---|
| `identity.user.registered` | Counter | — | Successful registrations |
| `identity.user.registration.failed` | Counter | `reason`: `duplicate_email`, `duplicate_username`, `weak_password` | Registration failures |
| `identity.user.deleted` | Counter | — | User deletions |

#### Auth Module — Login / Session
| Metric | Type | Tags | Description |
|---|---|---|---|
| `identity.auth.login.attempt` | Counter | `outcome`: `success`, `failure` | All login attempts |
| `identity.auth.login.failure` | Counter | `reason`: `invalid_credentials`, `user_not_found` | Login failures by cause |
| `identity.auth.token.refresh` | Counter | `outcome`: `success`, `failure` | Refresh token operations |
| `identity.auth.token.refresh.failure` | Counter | `reason`: `expired`, `revoked`, `invalid` | Refresh failures by cause |
| `identity.auth.logout` | Counter | — | Logout operations |
| `identity.auth.session.created` | Counter | — | New sessions |
| `identity.auth.session.revoked` | Counter | `reason`: `logout`, `password_reset`, `rotation_failure` | Session revocations |

#### Auth Module — Password Reset
| Metric | Type | Tags | Description |
|---|---|---|---|
| `identity.auth.password_reset.requested` | Counter | — | Reset requests (regardless of account existence) |
| `identity.auth.password_reset.completed` | Counter | — | Successful resets |
| `identity.auth.password_reset.failed` | Counter | `reason`: `invalid_token`, `expired_token` | Failed resets |

#### Infrastructure
| Metric | Type | Tags | Description |
|---|---|---|---|
| `identity.email.sent` | Counter | `outcome`: `success`, `failure`, `type`: `password_reset` | Email dispatch outcomes |

### 3.3 Implementation Pattern

Inject `IdentityMetrics` into each Application Service. Example:

```java
// LoginService
metrics.loginAttempt("success").increment();
// or
metrics.loginFailure("invalid_credentials").increment();
```

Keep metrics calls at the **application service layer** only. Never in domain or infrastructure adapters.

### 3.4 Already Available (No Code Needed)

Spring Boot Actuator + Micrometer auto-provides:
- `http.server.requests` — request count, duration, status per endpoint
- `jvm.memory.*`, `jvm.gc.*`, `jvm.threads.*` — JVM health
- `hikaricp.*` — DB connection pool utilization
- `spring.data.repository.invocations.*` — JPA repository call counts and timing

---

## Phase 4 — Infrastructure (docker-compose)

### 4.1 Add to docker-compose.yml

```yaml
# Grafana Tempo — Distributed Trace Backend
tempo:
  image: grafana/tempo:latest
  command: ["-config.file=/etc/tempo.yaml"]
  volumes:
    - ./infra/tempo/tempo.yaml:/etc/tempo.yaml
    - tempo-data:/var/tempo
  ports:
    - "3200:3200"   # Tempo API (Grafana queries here)
    - "4318:4318"   # OTLP HTTP receiver (services send traces here)

# Grafana Loki — Log Aggregation Backend
loki:
  image: grafana/loki:latest
  command: ["-config.file=/etc/loki/loki.yaml"]
  volumes:
    - ./infra/loki/loki.yaml:/etc/loki/loki.yaml
    - loki-data:/loki
  ports:
    - "3100:3100"

# Promtail — Log Shipper (reads Docker container logs → Loki)
promtail:
  image: grafana/promtail:latest
  command: ["-config.file=/etc/promtail/promtail.yaml"]
  volumes:
    - ./infra/promtail/promtail.yaml:/etc/promtail/promtail.yaml
    - /var/lib/docker/containers:/var/lib/docker/containers:ro
    - /var/run/docker.sock:/var/run/docker.sock
  depends_on:
    - loki

volumes:
  tempo-data:
  loki-data:
```

### 4.2 Tempo Configuration

Create `infra/tempo/tempo.yaml`:
```yaml
server:
  http_listen_port: 3200

distributor:
  receivers:
    otlp:
      protocols:
        http:
          endpoint: "0.0.0.0:4318"

storage:
  trace:
    backend: local
    local:
      path: /var/tempo/traces
    wal:
      path: /var/tempo/wal
```

### 4.3 Loki Configuration

Create `infra/loki/loki.yaml`:
```yaml
auth_enabled: false

server:
  http_listen_port: 3100

ingester:
  lifecycler:
    address: 127.0.0.1
    ring:
      kvstore:
        store: inmemory
      replication_factor: 1
    final_sleep: 0s

schema_config:
  configs:
    - from: 2024-01-01
      store: tsdb
      object_store: filesystem
      schema: v13
      index:
        prefix: index_
        period: 24h

storage_config:
  tsdb_shipper:
    active_index_directory: /loki/index
    cache_location: /loki/index_cache
  filesystem:
    directory: /loki/chunks
```

### 4.4 Promtail Configuration

Create `infra/promtail/promtail.yaml`:
```yaml
server:
  http_listen_port: 9080

positions:
  filename: /tmp/positions.yaml

clients:
  - url: http://loki:3100/loki/api/v1/push

scrape_configs:
  - job_name: docker
    docker_sd_configs:
      - host: unix:///var/run/docker.sock
        refresh_interval: 5s
    relabel_configs:
      - source_labels: ['__meta_docker_container_name']
        regex: '/(.*)'
        target_label: 'container'
      - source_labels: ['__meta_docker_container_label_com_docker_compose_service']
        target_label: 'service'
    pipeline_stages:
      - json:
          expressions:
            level: level
            traceId: traceId
            spanId: spanId
            message: message
      - labels:
          level:
          traceId:
          spanId:
```

### 4.5 Grafana Data Source Provisioning

Create `infra/grafana/provisioning/datasources/datasources.yaml`:
```yaml
apiVersion: 1
datasources:
  - name: Prometheus
    type: prometheus
    url: http://prometheus:9090
    isDefault: true

  - name: Loki
    type: loki
    url: http://loki:3100
    jsonData:
      derivedFields:
        - datasourceUid: tempo
          matcherRegex: '"traceId":"(\w+)"'
          name: TraceID
          url: '$${__value.raw}'

  - name: Tempo
    type: tempo
    uid: tempo
    url: http://tempo:3200
    jsonData:
      tracesToLogsV2:
        datasourceUid: loki
        filterByTraceID: true
      serviceMap:
        datasourceUid: prometheus
```

This wiring enables:
- Click a `traceId` in a log line → jump to trace in Tempo
- Click a trace span → see correlated logs in Loki
- Service map in Grafana from Prometheus metrics

---

## Phase 5 — Grafana Dashboards

Provision as JSON files in `infra/grafana/provisioning/dashboards/`.

### Dashboard 1: Identity Service — Health

Panels:
- HTTP request rate by endpoint (PromQL: `rate(http_server_requests_seconds_count{...}[1m])`)
- HTTP error rate 4xx/5xx
- p95 response latency by endpoint
- JVM heap used vs committed
- GC pause duration
- DB connection pool active/idle/pending
- Hikari pool timeout rate

### Dashboard 2: Identity Service — Business Metrics

Panels:
- Registrations per minute
- Login success vs failure rate (stacked)
- Login failure breakdown by reason (pie or bar)
- Active sessions (derived from created − revoked)
- Token refresh rate and failure rate
- Password reset funnel: requested → completed → failed
- Email send success/failure rate

### Dashboard 3: Identity Service — Errors & Security

Panels:
- Top 5 error types (by exception class from `http.server.requests` tag)
- Error log stream (Loki query filtered to `level=ERROR, service=identity-service`)
- Login failure spike detector (rate-of-rate)
- Password reset token failures (potential abuse signal)
- Trace explorer link (jump to Tempo from dashboard)

---

## Phase 6 — Prometheus Alerting Rules

Create `infra/prometheus/rules/identity-alerts.yaml`:

```yaml
groups:
  - name: identity-service
    rules:

      - alert: IdentityServiceDown
        expr: up{job="identity-service"} == 0
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "Identity service is unreachable"

      - alert: HighLoginFailureRate
        expr: |
          rate(identity_auth_login_failure_total[5m]) /
          rate(identity_auth_login_attempt_total[5m]) > 0.3
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "Login failure rate > 30% — possible credential stuffing"

      - alert: PasswordResetAbuse
        expr: rate(identity_auth_password_reset_requested_total[5m]) > 10
        for: 1m
        labels:
          severity: warning
        annotations:
          summary: "Password reset rate spike — possible enumeration attack"

      - alert: HighHttpErrorRate
        expr: |
          rate(http_server_requests_seconds_count{status=~"5..",job="identity-service"}[5m]) > 1
        for: 2m
        labels:
          severity: critical
        annotations:
          summary: "5xx error rate > 1/s on identity-service"

      - alert: SlowResponses
        expr: |
          histogram_quantile(0.95,
            rate(http_server_requests_seconds_bucket{job="identity-service"}[5m])
          ) > 2
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "p95 latency > 2s on identity-service"

      - alert: DBConnectionPoolExhausted
        expr: hikaricp_connections_pending{pool="HikariPool-1"} > 5
        for: 1m
        labels:
          severity: critical
        annotations:
          summary: "DB connection pool pending queue > 5 — possible connection leak"
```

Add to `prometheus.yml`:
```yaml
rule_files:
  - /etc/prometheus/rules/*.yaml
```

---

## Implementation Steps for the Implementer

Work in this order. Each step is independently verifiable.

### Step 1 — Structured Logging
1. Add `logstash-logback-encoder` dependency to identity-service pom.xml
2. Create `logback-spring.xml` (JSON profile + local plaintext profile)
3. Create `IdentityMdcFilter` — inject `userId` from `X-User-Id` header
4. Add log statements to all 9 application services (see log-level policy above)
5. **Verify:** Run `./mvnw test -pl services/identity-service`; start with `docker-compose up`; call login endpoint; confirm log line contains `traceId`, `spanId`, `userId` fields in JSON

### Step 2 — Distributed Tracing
1. Add `micrometer-tracing-bridge-otel` + `opentelemetry-exporter-otlp` to pom.xml
2. Add tracing config block to `application.yml` (sampling + OTLP endpoint)
3. Add Tempo service to `docker-compose.yml` + `infra/tempo/tempo.yaml`
4. **Verify:** Call login endpoint; open Grafana → Explore → Tempo; search by service name; confirm trace with child spans for HTTP + DB

### Step 3 — Business Metrics
1. Create `IdentityMetrics` bean in `shared/infrastructure/metrics/`
2. Inject into each application service; add increment calls at appropriate points
3. **Verify:** Call `GET /actuator/prometheus`; confirm custom metric names appear; call login with bad credentials; confirm `identity_auth_login_failure_total` increments

### Step 4 — Log Aggregation
1. Add Loki + Promtail to `docker-compose.yml`
2. Create `infra/loki/loki.yaml` and `infra/promtail/promtail.yaml`
3. **Verify:** `docker-compose up loki promtail`; generate log traffic; open Grafana → Explore → Loki; query `{service="identity-service"}`; confirm logs appear

### Step 5 — Grafana Data Sources & Dashboards
1. Create Grafana provisioning directory structure under `infra/grafana/`
2. Add datasources YAML (Prometheus, Loki, Tempo with cross-linking)
3. Create three dashboard JSON files (Health, Business, Errors)
4. Update `docker-compose.yml` Grafana volumes to mount provisioning dir
5. **Verify:** Restart Grafana; confirm all 3 data sources show "Connected"; open each dashboard; verify panels load

### Step 6 — Alerting Rules
1. Create `infra/prometheus/rules/identity-alerts.yaml`
2. Update `prometheus.yml` with `rule_files` entry
3. **Verify:** Restart Prometheus; go to `http://localhost:9090/rules`; confirm all 6 rules in PENDING or OK state

---

## Files to Create / Modify

### New Files
```
services/identity-service/src/main/resources/logback-spring.xml
services/identity-service/src/main/java/com/tickon/identity/shared/infrastructure/web/IdentityMdcFilter.java
services/identity-service/src/main/java/com/tickon/identity/shared/infrastructure/metrics/IdentityMetrics.java
infra/tempo/tempo.yaml
infra/loki/loki.yaml
infra/promtail/promtail.yaml
infra/grafana/provisioning/datasources/datasources.yaml
infra/grafana/provisioning/dashboards/identity-health.json
infra/grafana/provisioning/dashboards/identity-business.json
infra/grafana/provisioning/dashboards/identity-errors.json
infra/prometheus/rules/identity-alerts.yaml
```

### Modified Files
```
services/identity-service/pom.xml                    — add 3 dependencies
services/identity-service/src/main/resources/application.yml  — tracing config
docker-compose.yml                                   — add tempo, loki, promtail; grafana volumes
prometheus.yml                                       — add rule_files
services/identity-service/src/main/java/com/tickon/identity/user/application/services/RegisterUserService.java
services/identity-service/src/main/java/com/tickon/identity/user/application/services/GetUserByIdService.java
services/identity-service/src/main/java/com/tickon/identity/user/application/services/DeleteUserService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/LoginService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/RefreshTokenService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/LogoutService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/RequestPasswordResetService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/VerifyResetTokenService.java
services/identity-service/src/main/java/com/tickon/identity/auth/application/services/ResetPasswordService.java
services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/event/SendPasswordResetEmailHandler.java
services/identity-service/src/main/java/com/tickon/identity/auth/infrastructure/event/RevokeSessionsOnPasswordResetHandler.java
```

---

## Tests the Implementer Must Write

Per project TDD policy, write tests before or alongside each step:

| Test | Type | Location |
|---|---|---|
| `IdentityMdcFilterTest` — MDC fields populated from header | Unit | `shared/infrastructure/web/` |
| `IdentityMetricsTest` — counters increment on service calls | Unit (mock MeterRegistry) | `shared/infrastructure/metrics/` |
| `LoginServiceMetricsTest` — failure reason tags recorded correctly | Unit | `auth/application/services/` |
| `RegisterUserServiceMetricsTest` — success/failure counters | Unit | `user/application/services/` |

No tests needed for logback config or docker-compose infra — those are verified manually.

---

## Decisions & Rationale

**Why Tempo instead of Zipkin?**
Tempo is the native Grafana tracing backend. Zero-config correlation between traces, logs (Loki), and metrics (Prometheus) inside Grafana. Zipkin is standalone and requires more glue. Tempo also scales from local (filesystem) to production (S3/GCS) with a config change.

**Why Loki instead of Elasticsearch?**
Loki indexes only labels (not full text), making it orders of magnitude cheaper to run. For a microservices project like this, label-based filtering on `service`, `level`, `traceId` is sufficient and performant.

**Why `logstash-logback-encoder` instead of Logback's built-in JSON?**
It produces Logstash-compatible JSON with Micrometer-standard field names (`traceId`, `spanId`) automatically, and supports custom fields and MDC natively.

**Why `IdentityMetrics` as a centralized bean instead of scattered `MeterRegistry` calls?**
Centralizes metric names (no typos across files), makes metrics testable in isolation, and makes the full metric catalog immediately visible to future maintainers.

**Why keep metrics at the application service layer?**
Application services are the source of business truth. Domain objects should not know about observability infrastructure (hexagonal architecture rule: domain has zero outward dependencies). Infrastructure adapters are too low-level.

---

## Verification Checklist (End-to-End)

After completing all steps:

- [ ] `./mvnw clean verify` passes
- [ ] `docker-compose up` — all services healthy
- [ ] Login request → JSON log appears in Grafana/Loki with traceId
- [ ] TraceId in Loki log is clickable and opens full trace in Grafana/Tempo
- [ ] Trace in Tempo shows HTTP span + DB child spans
- [ ] `GET /actuator/prometheus` returns `identity_auth_login_attempt_total` and other custom metrics
- [ ] Grafana Health dashboard shows JVM, HTTP, and DB pool panels with data
- [ ] Grafana Business dashboard shows login success/failure counters
- [ ] Failed login increments `identity_auth_login_failure_total{reason="invalid_credentials"}`
- [ ] Prometheus rules page shows all 6 alert rules loaded
- [ ] Bad credentials in a loop triggers `HighLoginFailureRate` alert to FIRING state in Prometheus
