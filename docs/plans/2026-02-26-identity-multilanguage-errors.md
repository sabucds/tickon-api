# identity-service multilanguage domain errors
Status: done
Service/module: identity-service

## Goal
Translate domain error messages based on the client's `Accept-Language` header.
All 15 `IdentityExceptionCodes` must return localized messages in `ApiError.message`.

## Scope / Non-goals
- **In scope**: domain exception messages returned by `IdentityGlobalExceptionHandler`.
- **Out of scope (next phases)**:
  - `GlobalExceptionHandler` in `common` (VALIDATION_FAILED, ACCESS_DENIED, INTERNAL_ERROR).
  - Bean Validation field-level messages (`ValidationMessages.properties`).
  - Languages beyond the first two (English + one other).

## Touchpoints (files/packages)

| Action | File |
|--------|------|
| NEW    | `identity-service/src/main/resources/messages.properties` |
| NEW    | `identity-service/src/main/resources/messages_es.properties` |
| NEW    | `bootstrap/config/MessageSourceConfig.java` |
| MODIFY | `shared/platform/web/IdentityGlobalExceptionHandler.java` |

Domain layer: **no changes**.

## Design decisions
- **Translation at the infrastructure boundary**: `IdentityGlobalExceptionHandler` is the only place that needs to change; the domain stays Spring-free.
- **Key convention**: `"identity.error." + code.name()` (e.g., `identity.error.INVALID_CREDENTIALS`). Derived at runtime — no enum changes.
- **Locale resolution**: `AcceptHeaderLocaleResolver` bean reads the `Accept-Language` header and populates `LocaleContextHolder` automatically.
- **Fallback**: `MessageSource` configured with `setUseCodeAsDefaultMessage(true)` — missing keys return the code string, never throw.
- **`InvalidPasswordException` free-form message**: still used for server-side logging; the API response now uses the bundle key `identity.error.INVALID_PASSWORD`.

## Tests to write first
1. **Unit** — `IdentityGlobalExceptionHandlerTest`: mock `MessageSource`; assert that for a given `IdentityDomainException` and locale, the handler returns the expected translated message.
2. **Integration / slice** — `@WebMvcTest` on any controller; set `Accept-Language: es` header; assert `ApiError.message` matches the Spanish bundle value.

## Steps (execute 2–4 at a time)

1. Write failing unit test for the handler (locale resolution + message lookup).
2. Write failing `@WebMvcTest` slice test (end-to-end header → translated message).
3. Create `MessageSourceConfig`: `ReloadableResourceBundleMessageSource` + `AcceptHeaderLocaleResolver`.
4. Create `messages.properties` (English, all 15 codes) and `messages_es.properties`.
5. Update `IdentityGlobalExceptionHandler`: inject `MessageSource`, replace `ex.getMessage()` with `messageSource.getMessage("identity.error." + ex.code().name(), null, LocaleContextHolder.getLocale())`.
6. Run all tests green.

## Verification (exact mvn commands)
```bash
./mvnw test -pl services/identity-service
./mvnw clean verify
```

## Progress log (brief)
- 2026-02-26: Plan written.
- 2026-02-26: Implementation complete; tests green per local run.
