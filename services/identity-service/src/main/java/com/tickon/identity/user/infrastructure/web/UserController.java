package com.tickon.identity.user.infrastructure.web;

import com.tickon.common.commands.CommandBus;
import com.tickon.common.exceptions.GlobalExceptionHandler.ApiError;
import com.tickon.common.queries.QueryBus;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.command.delete.DeleteUserCommand;
import com.tickon.identity.user.application.query.getuserbyid.GetUserByIdQuery;
import com.tickon.identity.user.domain.valueobjects.UserId;
import com.tickon.identity.user.infrastructure.web.dto.RegisterUserRequest;
import com.tickon.identity.user.infrastructure.web.dto.UserResponse;
import com.tickon.identity.user.infrastructure.web.mappers.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Users", description = "User registration and profile management")
@RestController
@RequestMapping("/v1/users")
public class UserController {

  private final CommandBus commandBus;
  private final QueryBus queryBus;

  UserController(CommandBus commandBus, QueryBus queryBus) {
    this.commandBus = commandBus;
    this.queryBus = queryBus;
  }

  @Operation(summary = "Register a new user", security = {})
  @ApiResponse(responseCode = "201", description = "User created successfully")
  @ApiResponse(responseCode = "400",
      description = "VALIDATION_FAILED | INVALID_EMAIL | INVALID_PASSWORD | INVALID_USERNAME — see error code in response",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "409",
      description = "DUPLICATE_EMAIL | DUPLICATE_USERNAME — account with given email or username already exists",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping
  public UserResponse register(@Valid @RequestBody RegisterUserRequest request) {
    UserResult result = commandBus.execute(UserMapper.toRegisterCommand(request)).orElseThrow();
    return UserMapper.toDto(result);
  }

  @Operation(summary = "Get the currently authenticated user's profile",
      security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "User profile returned")
  @ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS — missing or invalid JWT",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "403", description = "ACCESS_DENIED",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND — user not found",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @GetMapping("me")
  public ResponseEntity<UserResponse> getCurrentUser(
      @Parameter(hidden = true) @RequestHeader("X-User-Id") String userId) {
    return queryBus.<Optional<UserResult>>execute(new GetUserByIdQuery(UserId.from(userId))).orElseThrow()
        .map(UserMapper::toDto).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get a user by ID", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "200", description = "User found")
  @ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS — missing or invalid JWT",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND — user not found")
  @GetMapping("/{id}")
  public Optional<UserResponse> getUser(@PathVariable String id) {
    return queryBus.<Optional<UserResult>>execute(new GetUserByIdQuery(UserId.from(id))).orElseThrow()
        .map(UserMapper::toDto);
  }

  @Operation(summary = "Delete a user by ID", security = @SecurityRequirement(name = "bearerAuth"))
  @ApiResponse(responseCode = "204", description = "User deleted successfully")
  @ApiResponse(responseCode = "401", description = "INVALID_CREDENTIALS — missing or invalid JWT",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "403", description = "ACCESS_DENIED",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "404", description = "RESOURCE_NOT_FOUND — user not found",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ApiResponse(responseCode = "500", description = "INTERNAL_ERROR",
      content = @Content(schema = @Schema(implementation = ApiError.class)))
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @DeleteMapping("/{id}")
  public void deleteUser(@PathVariable String id) {
    commandBus.execute(new DeleteUserCommand(UserId.from(id)));
  }
}
