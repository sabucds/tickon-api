# current-user endpoint
Status: done
Service/module: api-gateway (filter), identity-service/user (controller)

## Goal
`GET /api/identity/v1/users/me` returns the authenticated user's profile.

## Scope / Non-goals
- IN: add endpoint, gateway header relay, tests
- OUT: change auth model, new use case (reuse `GetUserByIdUseCase`)

## How it works
1. Client sends `Authorization: Bearer <jwt>`
2. API Gateway validates JWT (already done)
3. New `UserIdRelayFilter` strips any incoming `X-User-Id` (prevent spoofing) then adds `X-User-Id: <sub>` from validated JWT
4. Identity service `GET /v1/users/me` reads `X-User-Id` header, calls `GetUserByIdUseCase`
5. Returns `UserResponse` (200) or 404

## Touchpoints
- `services/api-gateway/src/main/java/com/tickon/gateway/filters/UserIdRelayFilter.java` (new)
- `services/api-gateway/src/test/java/com/tickon/gateway/filters/UserIdRelayFilterTest.java` (new)
- `services/identity-service/.../user/infrastructure/web/UserController.java` (add `/me` endpoint)
- `services/identity-service/.../user/infrastructure/web/UserControllerTest.java` (add tests)

## Tests to write first
1. `UserControllerTest` – `shouldReturnCurrentUser_WhenHeaderPresent` (200 + user fields)
2. `UserControllerTest` – `shouldReturn404_WhenUserNotFound` (header present, use case returns empty)
3. `UserIdRelayFilterTest` – `shouldAddUserIdHeader_WhenAuthenticated`
4. `UserIdRelayFilterTest` – `shouldStripIncomingUserIdHeader_WhenUnauthenticated`

## Steps
1. Write failing controller tests → add `GET /v1/users/me` to `UserController`
2. Write failing filter tests → add `UserIdRelayFilter` to api-gateway
3. Run `./mvnw clean verify`

## Verification
```
./mvnw clean verify
./mvnw test -pl services/identity-service
./mvnw test -pl services/api-gateway
```

## Progress log
- [x] Step 1: controller test + endpoint
- [x] Step 2: filter test + filter — note: `switchIfEmpty(chain.filter(x))` is a reactive trap; `chain.filter()` is eager (calls lambda immediately). Fixed by restructuring to `.filter().cast().flatMap().defaultIfEmpty(stripped).flatMap(chain::filter)`
- [x] Step 3: verify — BUILD SUCCESS, 230 tests passing
