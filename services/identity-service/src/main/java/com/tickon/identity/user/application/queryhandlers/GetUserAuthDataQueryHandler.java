package com.tickon.identity.user.application.queryhandlers;

import com.tickon.common.identity.domain.valueobjects.UserId;
import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.shared.contracts.queries.GetUserAuthDataQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GetUserAuthDataQueryHandler implements QueryHandler<GetUserAuthDataQuery, UserAuthDataDTO> {
  private static final Logger log = LoggerFactory.getLogger(GetUserAuthDataQueryHandler.class);

  private final UserRepository userRepository;

  public GetUserAuthDataQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<UserAuthDataDTO> handle(GetUserAuthDataQuery query) {
    try {
      return userRepository.findById(new UserId(query.userId())).map(this::toDTO)
          .<QueryResult<UserAuthDataDTO>>map(QueryResult.Success::new).orElseGet(() -> {
            log.debug("User not found for id: {}", query.userId());
            return new QueryResult.NotFound<>();
          });
    } catch (Exception e) {
      log.error("Failed to fetch user auth data for id: {}", query.userId(), e);
      return new QueryResult.Error<>("Failed to fetch user auth data", e);
    }
  }

  @Override
  public Class<GetUserAuthDataQuery> getQueryClass() {
    return GetUserAuthDataQuery.class;
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
