package com.tickon.identity.user.infrastructure.web;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.identity.user.application.ports.in.DeleteUserUseCase;
import com.tickon.identity.user.application.ports.in.GetUserByIdUseCase;
import com.tickon.identity.user.application.ports.in.RegisterUserUseCase;
import com.tickon.identity.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identity.user.infrastructure.web.dto.UserResponse;
import com.tickon.identity.user.infrastructure.web.mappers.UserMapper;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final RegisterUserUseCase registerUser;
  private final GetUserByIdUseCase getUserById;
  private final DeleteUserUseCase deleteUser;

  UserController(RegisterUserUseCase registerUser, GetUserByIdUseCase getUserById, DeleteUserUseCase deleteUser) {
    this.registerUser = registerUser;
    this.getUserById = getUserById;
    this.deleteUser = deleteUser;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    var appResponse = registerUser.register(UserMapper.toRegisterCommand(request));
    return UserMapper.toDto(appResponse);
  }

  @GetMapping("me")
  public ResponseEntity<UserResponse> getCurrentUser(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
    return getUserById.handle(UserId.from(userId)).map(UserMapper::toDto).map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public Optional<UserResponse> getUser(@PathVariable String id) {
    return getUserById.handle(UserId.from(id)).map(UserMapper::toDto);

  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable String id) {
    deleteUser.handle(UserId.from(id));
  }
}
