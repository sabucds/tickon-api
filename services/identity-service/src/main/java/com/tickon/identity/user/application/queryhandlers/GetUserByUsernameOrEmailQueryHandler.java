package com.tickon.identity.user.application.queryhandlers;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.shared.contracts.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetUserByUsernameOrEmailQueryHandler
    implements QueryHandler<GetUserByUsernameOrEmailQuery, UserAuthDataDTO> {
  private static final Logger log = LoggerFactory.getLogger(GetUserByUsernameOrEmailQueryHandler.class);

  private final UserRepository userRepository;

  public GetUserByUsernameOrEmailQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<UserAuthDataDTO> handle(GetUserByUsernameOrEmailQuery query) {
    try {
      return userRepository.findByUsernameOrEmail(query.usernameOrEmail()).map(this::toDTO)
          .<QueryResult<UserAuthDataDTO>>map(QueryResult.Success::new).orElseGet(() -> {
            log.debug("User not found for username/email: {}", query.usernameOrEmail());
            return new QueryResult.NotFound<>();
          });
    } catch (Exception e) {
      log.error("Failed to fetch user by username/email: {}", query.usernameOrEmail(), e);
      return new QueryResult.Error<>("Failed to fetch user by username/email", e);
    }
  }

  @Override
  public Class<GetUserByUsernameOrEmailQuery> getQueryClass() {
    return GetUserByUsernameOrEmailQuery.class;
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
