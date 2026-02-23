package com.tickon.identity.contracts.user.queries;

import java.util.UUID;

// Cross-module contract: result type for queries dispatched from auth/ to user/ via QueryBus.
public record UserAuthDataDTO(UUID id, String passwordHash, String status) {}
