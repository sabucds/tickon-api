package com.tickon.identity.shared.contracts.queries;

import java.util.UUID;

public record UserAuthDataDTO(UUID id, String passwordHash, String status) {}
