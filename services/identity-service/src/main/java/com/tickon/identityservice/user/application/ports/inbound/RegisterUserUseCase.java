// user/application/port/in/RegisterUserUseCase.java
package com.tickon.identityservice.user.application.ports.inbound;

import com.tickon.identityservice.user.application.models.RegisterUserRequest;
import com.tickon.identityservice.user.application.models.UserResponse;

public interface RegisterUserUseCase {
  UserResponse register(RegisterUserRequest request);
}
