package com.tickon.identity.user.application.query.getuserbyid;

import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.application.ports.UserRepository;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetUserByIdQueryHandler implements QueryHandler<GetUserByIdQuery, Optional<UserResult>> {
  private static final Logger log = LoggerFactory.getLogger(GetUserByIdQueryHandler.class);

  private final UserRepository userRepository;

  public GetUserByIdQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<Optional<UserResult>> handle(GetUserByIdQuery query) {
    Optional<UserResult> result = userRepository.findById(query.userId()).map(UserResult::from);
    if (result.isPresent()) {
      log.debug("User found: userId={}", query.userId().value());
    } else {
      log.debug("User not found: userId={}", query.userId().value());
    }
    return new QueryResult.Success<>(result);
  }

  @Override
  public Class<GetUserByIdQuery> getQueryClass() {
    return GetUserByIdQuery.class;
  }
}
