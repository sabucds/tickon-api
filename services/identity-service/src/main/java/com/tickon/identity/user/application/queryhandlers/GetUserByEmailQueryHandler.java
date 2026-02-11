package com.tickon.identity.user.application.queryhandlers;

import com.tickon.common.identity.domain.valueobjects.Email;
import com.tickon.common.queries.QueryHandler;
import com.tickon.common.queries.QueryResult;
import com.tickon.identity.shared.contracts.queries.GetUserByEmailQuery;
import com.tickon.identity.shared.contracts.queries.UserAuthDataDTO;
import com.tickon.identity.user.application.ports.out.UserRepository;
import com.tickon.identity.user.domain.User;
import org.springframework.stereotype.Component;

@Component
public class GetUserByEmailQueryHandler implements QueryHandler<GetUserByEmailQuery, UserAuthDataDTO> {

  private final UserRepository userRepository;

  public GetUserByEmailQueryHandler(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public QueryResult<UserAuthDataDTO> handle(GetUserByEmailQuery query) {
    return userRepository.findByEmail(Email.from(query.email())).map(this::toDTO)
        .<QueryResult<UserAuthDataDTO>>map(QueryResult.Success::new).orElseGet(() -> new QueryResult.NotFound<>());
  }

  @Override
  public Class<GetUserByEmailQuery> getQueryClass() {
    return GetUserByEmailQuery.class;
  }

  private UserAuthDataDTO toDTO(User user) {
    return new UserAuthDataDTO(user.id().value(), user.passwordHash().value(), user.status().name());
  }
}
