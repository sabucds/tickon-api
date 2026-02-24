package com.tickon.identity.user.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.command.delete.DeleteUserCommand;
import com.tickon.identity.user.application.query.getuserbyid.GetUserByIdQuery;
import com.tickon.identity.user.domain.valueobjects.UserId;
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

  private final CommandBus commandBus;
  private final QueryBus queryBus;

  UserController(CommandBus commandBus, QueryBus queryBus) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    UserResult result = commandBus.execute(UserMapper.toRegisterCommand(request)).orElseThrow();
    return UserMapper.toDto(result);
  }

  @GetMapping("me")
  public ResponseEntity<UserResponse> getCurrentUser(
      @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
    return queryBus.<Optional<UserResult>>execute(new GetUserByIdQuery(UserId.from(userId))).orElseThrow()
        .map(UserMapper::toDto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @GetMapping("/{id}")
  public Optional<UserResponse> getUser(@PathVariable String id) {
    return queryBus.<Optional<UserResult>>execute(new GetUserByIdQuery(UserId.from(id))).orElseThrow()
        .map(UserMapper::toDto);
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable String id) {
    commandBus.execute(new DeleteUserCommand(UserId.from(id)));
  }
}
