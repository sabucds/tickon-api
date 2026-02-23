package com.tickon.identity.user.application.queryhandlers;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.contracts.user.queries.GetUserByUsernameOrEmailQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetUserByUsernameOrEmailQueryHandler
    implements QueryHandler<GetUserByUsernameOrEmailQuery, Optional<UserAuthDataDTO>> {
  private static final Logger log = LoggerFactory.getLogger(GetUserByUsernameOrEmailQueryHandler.class);

  private final UserRepository userRepository;

  public GetUserByUsernameOrEmailQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<Optional<UserAuthDataDTO>> handle(GetUserByUsernameOrEmailQuery query) {
    Optional<UserAuthDataDTO> userOpt = userRepository.findByUsernameOrEmail(query.usernameOrEmail()).map(this::toDTO);
    if (userOpt.isEmpty()) {
      log.debug("User not found for username/email: {}", query.usernameOrEmail());
    }
    return new QueryResult.Success<>(userOpt);
  }

  @Override
  public Class<GetUserByUsernameOrEmailQuery> getQueryClass() {
    return GetUserByUsernameOrEmailQuery.class;
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
