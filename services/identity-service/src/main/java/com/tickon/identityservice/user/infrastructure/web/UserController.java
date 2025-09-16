package com.tickon.identityservice.user.infrastructure.web;

import com.tickon.identityservice.user.application.ports.inbound.GetUserByIdService;
import com.tickon.identityservice.user.application.ports.inbound.RegisterUserService;
import com.tickon.identityservice.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identityservice.user.infrastructure.web.dto.UserResponse;
import com.tickon.identityservice.user.infrastructure.web.mappers.UserMapper;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final RegisterUserService registerUser;
  private final GetUserByIdService getUserById;

  UserController(RegisterUserService registerUser, GetUserByIdService getUserById) {
    this.registerUser = registerUser;
    this.getUserById = getUserById;
  }

  @PostMapping
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    var appResponse = registerUser.register(UserMapper.toCommand(request));
    return UserMapper.toDto(appResponse);
  }

  @GetMapping("/{id}")
  public UserResponse getUser(@PathVariable String id) {
    return getUserById
        .handle(id)
        .map(UserMapper::toDto)
        .orElseThrow(() -> new RuntimeException("User not found: " + id));
  }
}
