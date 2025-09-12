// user/infrastructure/rest-adapters/dto/RegisterUserRestRequest.java
package com.tickon.identityservice.user.infrastructure.adapters.dto;

public record RegisterUserRestRequest(
    String firstName, String lastName, String username, String email, String password) {}
