# identity-service remove hardcoded error messages
Status: planned
Service/module: identity-service

## Goal
Remove all hardcoded English strings from domain exception constructors.
Each exception carries only a stable code (and domain data if needed).
`PasswordStrengthPolicy` violations each get their own translation key.

## Audit: 13 unique hardcoded messages across 10 exception classes

| Message | Where hardcoded |
|---------|-----------------|
| "Invalid credentials" | `InvalidCredentialsException` constructor |
| "Invalid refresh token" | `InvalidRefreshTokenException` constructor |
| "Invalid or expired reset token" | `InvalidResetTokenException` constructor |
| "Session is already expired" | `SessionExpiredException` constructor |
| "Session is already revoked for a different reason" | `SessionRevokedException` constructor |
| "Email already in use" | `DuplicateEmailException` constructor |
| "Username already in use" | `DuplicateUsernameException` constructor |
| "Invalid email" | `InvalidEmailException` constructor |
| "Invalid username" | `InvalidUsernameException` constructor |
| "Password must be at least 8 characters long" | `PasswordStrengthPolicy` → `InvalidPasswordException(String)` |
| "Password must contain at least one uppercase letter" | same |
| "Password must contain at least one lowercase letter" | same |
| "Password must contain at least one digit" | same |
| "Password must contain at least one special character" | same |

## Design

### 1. `IdentityDomainException` — remove String param, add `messageKey()`
```java
protected IdentityDomainException(IdentityExceptionCodes code) {
    super(code.name()); // still useful in stack traces / logs
    this.code = code;
}

public String messageKey() {
    return "identity.error." + code.name();
}
```
`messageKey()` is pure Java — no Spring dependency. Subclasses override it when they need a finer-grained key.

### 2. `PasswordViolation` enum (new, `user/domain/exceptions/`)
```java
public enum PasswordViolation {
    TOO_SHORT, MISSING_UPPERCASE, MISSING_LOWERCASE, MISSING_DIGIT, MISSING_SPECIAL_CHAR
}
```

### 3. `InvalidPasswordException` — takes `PasswordViolation`, overrides `messageKey()`
```java
public class InvalidPasswordException extends IdentityDomainException {
    private final PasswordViolation violation;

    public InvalidPasswordException(PasswordViolation violation) {
        super(IdentityExceptionCodes.INVALID_PASSWORD);
        this.violation = violation;
    }

    public PasswordViolation violation() { return violation; }

    @Override
    public String messageKey() {
        return "identity.error.INVALID_PASSWORD." + violation.name();
    }
}
```

### 4. All other exceptions — drop String message, call `super(code)` only
No behavior change. Exceptions that carry domain data (email, username) keep those fields.

### 5. Handler — use `ex.messageKey()`
```java
String message = messageSource.getMessage(ex.messageKey(), null, LocaleContextHolder.getLocale());
```
Replaces the explicit `"identity.error." + code.name()` string build.

### 6. Bundles — add 5 new keys, remove generic `INVALID_PASSWORD`
New keys:
```properties
identity.error.INVALID_PASSWORD.TOO_SHORT=Password must be at least 8 characters long
identity.error.INVALID_PASSWORD.MISSING_UPPERCASE=Password must contain at least one uppercase letter
identity.error.INVALID_PASSWORD.MISSING_LOWERCASE=Password must contain at least one lowercase letter
identity.error.INVALID_PASSWORD.MISSING_DIGIT=Password must contain at least one digit
identity.error.INVALID_PASSWORD.MISSING_SPECIAL_CHAR=Password must contain at least one special character
```
Remove: `identity.error.INVALID_PASSWORD` (no longer reachable).

## Touchpoints (files)

| Action | File |
|--------|------|
| MODIFY | `shared/kernel/exceptions/IdentityDomainException.java` |
| NEW    | `user/domain/exceptions/PasswordViolation.java` |
| MODIFY | `user/domain/exceptions/InvalidPasswordException.java` |
| MODIFY | `user/domain/exceptions/DuplicateEmailException.java` |
| MODIFY | `user/domain/exceptions/DuplicateUsernameException.java` |
| MODIFY | `user/domain/exceptions/InvalidEmailException.java` |
| MODIFY | `user/domain/exceptions/InvalidUsernameException.java` |
| MODIFY | `auth/domain/exceptions/InvalidCredentialsException.java` |
| MODIFY | `auth/domain/exceptions/InvalidRefreshTokenException.java` |
| MODIFY | `auth/domain/exceptions/InvalidResetTokenException.java` |
| MODIFY | `auth/domain/exceptions/SessionExpiredException.java` |
| MODIFY | `auth/domain/exceptions/SessionRevokedException.java` |
| MODIFY | `user/domain/policies/PasswordStrengthPolicy.java` |
| MODIFY | `shared/platform/web/IdentityGlobalExceptionHandler.java` |
| MODIFY | `src/main/resources/messages.properties` |
| MODIFY | `src/main/resources/messages_es.properties` |

## Tests to write first

1. **`InvalidPasswordExceptionTest`** (unit) — for each `PasswordViolation`, assert `ex.messageKey()` returns the correct key string. Also assert `ex.code()` is `INVALID_PASSWORD`.
2. **`PasswordStrengthPolicyTest`** (unit) — for each rule violation, assert the thrown `InvalidPasswordException` carries the correct `PasswordViolation`. Also assert valid password passes without exception.
3. Existing `IdentityGlobalExceptionHandlerTest` — no changes needed (uses `InvalidCredentialsException()`, constructor stays no-arg).

## Steps (execute in order)

1. Write failing `InvalidPasswordExceptionTest` and `PasswordStrengthPolicyTest`.
2. Modify `IdentityDomainException`: drop String param, add `messageKey()`.
3. Create `PasswordViolation` enum.
4. Modify `InvalidPasswordException`: takes `PasswordViolation`, overrides `messageKey()`.
5. Modify `PasswordStrengthPolicy`: replace all `new InvalidPasswordException(String)` with `PasswordViolation` enum values.
6. Drop String constructor param from all remaining 8 exception classes.
7. Update `IdentityGlobalExceptionHandler`: `ex.messageKey()` instead of `"identity.error." + code.name()`.
8. Update `messages.properties` and `messages_es.properties`: add 5 keys, remove 1.
9. Run tests green.

## Verification
```bash
./mvnw test -pl services/identity-service
./mvnw clean verify
```

## Progress log (brief)
- 2026-02-26: Plan written.
