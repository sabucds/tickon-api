// user/adapter/web/UserController.java
package com.tickon.identityservice.user.infrastructure.adapters;

import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdUseCase;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserUseCase;
import com.tickon.identityservice.user.domain.UserId;
import com.tickon.identityservice.user.infrastructure.adapters.dto.RegisterUserRestRequest;
import com.tickon.identityservice.user.infrastructure.adapters.dto.UserRestResponse;
import com.tickon.identityservice.user.infrastructure.adapters.mappers.UserRestMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
class UserController {

  private final RegisterUserUseCase registerUser;
  private final GetUserByIdUseCase getUserById;
  private final UserRestMapper mapper = new UserRestMapper();

  UserController(RegisterUserUseCase registerUser, GetUserByIdUseCase getUserById) {
    this.registerUser = registerUser;
    this.getUserById = getUserById;
  }

  @PostMapping
  public UserRestResponse register(@RequestBody RegisterUserRestRequest request) {
    var appResponse = registerUser.register(mapper.toApplicationRequest(request));
    return mapper.toWebResponse(appResponse);
  }

  @GetMapping("/{id}")
  public UserRestResponse getUser(@PathVariable String id) {
    return getUserById
        .handle(UserId.from(id))
        .map(mapper::toWebResponse)
        .orElseThrow(() -> new RuntimeException("User not found: " + id));
  }
}
