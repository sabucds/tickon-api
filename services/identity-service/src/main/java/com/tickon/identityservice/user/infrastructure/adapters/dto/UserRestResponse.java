// user/infrastructure/rest-adapters/dto/UserRestResponse.java
package com.tickon.identityservice.user.infrastructure.adapters.dto;

public record UserRestResponse(
    String id, String firstName, String lastName, String username, String email) {}
