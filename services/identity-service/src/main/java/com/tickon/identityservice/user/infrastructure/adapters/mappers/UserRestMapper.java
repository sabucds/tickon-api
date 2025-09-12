// user/infrastructure/rest-adapters/dto-mappers/UserRestMapper.java
package com.tickon.identityservice.user.infrastructure.adapters.mappers;

import com.tickon.identityservice.user.application.models.RegisterUserRequest;
import com.tickon.identityservice.user.application.models.UserResponse;
import com.tickon.identityservice.user.domain.Email;
import com.tickon.identityservice.user.domain.Username;

public class UserRestMapper {

  public RegisterUserRequest toApplicationRequest(
      com.tickon.identityservice.user.infrastructure.adapters.dto.RegisterUserRestRequest webRequest) {
    return new RegisterUserRequest(webRequest.firstName(), webRequest.lastName(),
        new Username(webRequest.username()), new Email(webRequest.email()), webRequest.password());
  }

  public com.tickon.identityservice.user.infrastructure.adapters.dto.UserRestResponse toWebResponse(UserResponse appResponse) {
    return new com.tickon.identityservice.user.infrastructure.adapters.dto.UserRestResponse(appResponse.id().value().toString(), appResponse.firstName(),
        appResponse.lastName(), appResponse.username(), appResponse.email());
  }
}
