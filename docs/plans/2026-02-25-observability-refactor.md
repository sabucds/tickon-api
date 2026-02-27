# Observability Refactor
Status: Done
Service/module: identity-service (primary), event-service (phase 5)
Branch: refactor/observability

## Goal
Fix broken observability (logs never reach Loki structured, tracing-to-log links are dead),
introduce a bus observability decorator to centralize cross-cutting logging/timing, and
clean up the IdentityMetrics API. Then extend the same foundation to event-service.

## Scope / Non-goals
In scope:
- Structured JSON logging (logback-spring.xml was never created — logs are plaintext today)
- Fix Promtail label cardinality (traceId/spanId as Loki labels is an anti-pattern)
- Bus observability decorator (ObservableCommandBus / ObservableQueryBus) — replaces
  inline timing/logging in SpringCommandBus and handler structural logs
- IdentityMetrics API: all methods become void; consolidate redundant counters
- Tempo metrics_generator so Grafana service map works
- event-service: same logging + tracing foundation + EventMetrics bean

Out of scope:
- Alertmanager routing (alerts fire to nowhere — separate task)
- Moving business metrics from handlers to domain event listeners (deliberate decision,
  see note below)
- Production retention config / secret management

Note on metrics placement: the original plan deliberately kept business counters in
handlers because they have domain context (failure reason, entity IDs) the decorator
cannot access. The decorator covers structural concerns (dispatch timing, outcome type).
Business counters stay in handlers via IdentityMetrics. Revisit if/when domain events
carry enough context.

---

## Issues being fixed

| # | Issue | Impact |
|---|---|---|
| P0 | No logback-spring.xml — logs are plaintext; Promtail JSON pipeline fails silently | No structured labels in Loki; Tempo→Loki links dead |
| P1 | traceId + spanId as Loki labels — unbounded cardinality | Loki index/memory bloat per unique request |
| P2 | SpringCommandBus/QueryBus log at DEBUG — nothing appears in Loki | Blind to all command/query dispatch in production |
| P3 | IdentityMetrics returns Counter objects; callers call .increment() | Awkward API; loginAttempt+loginFailure are redundant counters |
| P4 | Tempo has no metrics_generator — Grafana service map is empty | Grafana service map panel unusable |
| P5 | event-service has no tracing, no JSON logs, no business metrics | Entire service is a blind spot |

---

## Architecture decision: bus decorator

The Spring*Bus implementations mix two concerns: dispatch and observability.
We extract observability into a decorator per the standard Spring pattern:

```
CommandBus (interface, in common/)
├── SpringCommandBus  @Component          — pure dispatch only
└── ObservableCommandBus  @Primary        — wraps SpringCommandBus via @Qualifier
      logs: INFO on dispatch/success, WARN on domain failure, ERROR on unexpected
      metrics: Timer platform.command.duration{command=X, outcome=success|failure}
```

Same for QueryBus → ObservableQueryBus.

Spring wiring: `@Primary` on the decorator + `@Qualifier("springCommandBus")` on the
injected delegate avoids circular dependency.

ADR required: changes cross-cutting bus wiring in shared/platform/bus/.

---

## Touchpoints

### Phase 1 — Structured JSON logging
- `services/identity-service/pom.xml` — add logstash-logback-encoder:7.4
- `services/identity-service/src/main/resources/logback-spring.xml` — NEW
- `services/identity-service/src/main/resources/application.yml`
  - remove `logging.pattern.correlation` (conflicts with JSON encoder)
- IdentityMdcFilter already exists at shared/platform/web/ — no change needed

### Phase 2 — Fix Promtail label cardinality
- `infra/promtail/promtail.yaml`
  - remove `traceId:` and `spanId:` from `labels:` block
  - keep `level:` only (low-cardinality)
  - traceId/spanId remain in the JSON body, queryable via `| json` in LogQL

### Phase 3 — Bus observability decorator
New files:
- `services/identity-service/src/main/java/com/tickon/identity/shared/platform/bus/ObservableCommandBus.java`
- `services/identity-service/src/main/java/com/tickon/identity/shared/platform/bus/ObservableQueryBus.java`

Modified:
- `SpringCommandBus.java` — remove timing, remove logCommandResult(), keep only dispatch
- `SpringQueryBus.java` — same
- All 7 handler files — remove structural logs (covered by decorator); keep domain-specific
  logs that add context beyond what the decorator captures (userId, sessionId, etc.)

Tests:
- `ObservableCommandBusTest.java` — NEW
- `ObservableQueryBusTest.java` — NEW

### Phase 4 — IdentityMetrics API cleanup
- `shared/platform/metrics/IdentityMetrics.java`
  - All tagged factory methods → void `increment*(String tag)` — call .increment() internally
  - Drop `loginAttempt(outcome)` — replaced by decorator Timer + `loginSuccess()` counter
  - Add `loginSuccess()` pre-registered counter (was covered by loginAttempt("success"))
  - Keep all other pre-registered counters as void `increment*()` methods
- All 7 handler call sites — update to void API
- `IdentityMetricsTest.java` — update assertions

### Phase 5 — Tempo metrics_generator
- `infra/tempo/tempo.yaml` — add metrics_generator block
- `docker-compose.yml` — add `--web.enable-remote-write-receiver` to Prometheus command

### Phase 6 — event-service observability
- `services/event-service/pom.xml` — add logstash-logback-encoder, micrometer-tracing-bridge-otel, opentelemetry-exporter-otlp
- `services/event-service/src/main/resources/logback-spring.xml` — NEW (same pattern, service name = event-service)
- `services/event-service/src/main/resources/application.yml`
  - add tracing + OTLP config (same as identity-service)
  - fix `ddl-auto: update` → `none`; add Flyway config
- `services/event-service/src/main/java/.../shared/platform/metrics/EventMetrics.java` — NEW
- `services/event-service/src/main/java/.../shared/platform/bus/` — ObservableCommandBus + ObservableQueryBus (copy, same code)
- Relevant event-service command handlers — inject EventMetrics

---

## Tests to write first (TDD)

### Phase 1
No unit test for logback-spring.xml — verified manually.
Existing `IdentityMdcFilterTest` — confirm it still passes after pom change.

### Phase 3 — ObservableCommandBus
```
ObservableCommandBusTest:
- should_LogInfo_When_CommandSucceeds
- should_LogWarn_When_CommandThrowsDomainException
- should_LogError_When_CommandThrowsUnexpectedException
- should_RecordTimerWith_SuccessOutcome_When_CommandSucceeds
- should_RecordTimerWith_FailureOutcome_When_CommandThrows
- should_RethrowException_After_Logging
```

Same set for `ObservableQueryBusTest`.

Use `SimpleMeterRegistry` for timer assertions. Mock the inner `CommandBus` delegate.

### Phase 4 — IdentityMetrics
Update `IdentityMetricsTest`:
- All methods are void → assert via `registry.find(...).counter().count()`
- `loginSuccess()` counter exists separately from loginAttempt

### Phase 6 — EventMetrics
- `EventMetricsTest` — mirrors IdentityMetricsTest pattern

---

## ObservableCommandBus sketch

```java
@Primary
@Component
public class ObservableCommandBus implements CommandBus {

  private static final Logger log = LoggerFactory.getLogger(ObservableCommandBus.class);

  private final CommandBus delegate;
  private final MeterRegistry registry;

  public ObservableCommandBus(
      @Qualifier("springCommandBus") CommandBus delegate,
      MeterRegistry registry) {
    this.delegate = delegate;
    this.registry = registry;
  }

  @Override
  public <R> CommandResult<R> execute(Command<R> command) {
    String name = command.getCommandName();
    log.info("Dispatching {}", name);
    long start = System.nanoTime();
    try {
      CommandResult<R> result = delegate.execute(command);
      long ms = elapsedMs(start);
      log.info("{} succeeded ({}ms)", name, ms);
      recordTimer(name, "success", ms);
      return result;
    } catch (RuntimeException ex) {
      long ms = elapsedMs(start);
      boolean isDomain = isDomainException(ex);
      if (isDomain) {
        log.warn("{} failed ({}ms): {}", name, ms, ex.getMessage());
      } else {
        log.error("{} failed unexpectedly ({}ms)", name, ms, ex);
      }
      recordTimer(name, "failure", ms);
      throw ex;
    }
  }

  private void recordTimer(String command, String outcome, long ms) {
    Timer.builder("platform.command.duration")
        .tag("command", command)
        .tag("outcome", outcome)
        .register(registry)
        .record(ms, TimeUnit.MILLISECONDS);
  }

  private long elapsedMs(long startNano) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNano);
  }

  private boolean isDomainException(RuntimeException ex) {
    // CommandExecutionException wraps the original; unwrap to check
    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
    return cause.getClass().getPackageName().contains(".domain.");
  }
}
```

`isDomainException` heuristic can be replaced by a marker interface on domain exceptions
if the heuristic proves fragile (worth an ADR).

---

## Verification

After each phase:
```bash
./mvnw test -pl services/identity-service     # phases 1, 3, 4
./mvnw test -pl services/event-service        # phase 6
./mvnw clean verify                           # before PR
```

Manual (docker-compose):
```bash
docker compose up --build

# Phase 1: confirm JSON logs
curl -X POST http://localhost:8082/api/identity/auth/login -d '{...}'
# → Grafana/Loki: {service="identity-service"} | json
#   expect: level, traceId, spanId, message as fields in log body (not labels)

# Phase 2: confirm traceId NOT a Loki label
# → Grafana/Loki: label browser — only "level", "service", "container" should appear

# Phase 3: confirm bus timer metric
curl http://localhost:8082/actuator/prometheus | grep platform_command_duration

# Phase 4: confirm new metric names
curl http://localhost:8082/actuator/prometheus | grep identity_auth_login

# Phase 5: Grafana → Explore → Tempo → Service Map tab → expect nodes

# Phase 6: event-service same as identity-service
curl http://localhost:8081/actuator/prometheus | grep platform_command_duration
```

---

## Progress log
- [x] Phase 1: Structured JSON logging (identity-service)
- [x] Phase 2: Promtail label cardinality fix
- [ ] Phase 3: Bus observability decorator + handler cleanup
- [ ] Phase 4: IdentityMetrics API cleanup
- [ ] Phase 5: Tempo metrics_generator
- [ ] Phase 6: event-service observability
