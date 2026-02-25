package com.tickon.identity.user.application.query.getauthdata;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.contracts.user.queries.GetUserAuthDataQuery;
import com.tickon.identity.contracts.user.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.UserRepository;
import com.tickon.identity.user.domain.User;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetUserAuthDataQueryHandler implements QueryHandler<GetUserAuthDataQuery, Optional<UserAuthDataDTO>> {
  private static final Logger log = LoggerFactory.getLogger(GetUserAuthDataQueryHandler.class);

  private final UserRepository userRepository;

  public GetUserAuthDataQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<Optional<UserAuthDataDTO>> handle(GetUserAuthDataQuery query) {
    Optional<UserAuthDataDTO> userOpt = userRepository.findById(new UserId(query.userId())).map(this::toDTO);
    if (userOpt.isEmpty()) {
      log.debug("User not found for id: {}", query.userId());
    }
    return new QueryResult.Success<>(userOpt);
  }

  @Override
  public Class<GetUserAuthDataQuery> getQueryClass() {
    return GetUserAuthDataQuery.class;
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
