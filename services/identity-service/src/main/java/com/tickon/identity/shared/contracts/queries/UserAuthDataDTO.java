package com.tickon.identity.shared.contracts.queries;

import java.util.UUID;

/**
 * DTO containing only authentication-related user data. This is NOT a domain
 * object - it's a data transfer contract. Only contains the minimal fields
 * needed for authentication.
 */
public record UserAuthDataDTO(UUID id, String passwordHash, String status) {}
