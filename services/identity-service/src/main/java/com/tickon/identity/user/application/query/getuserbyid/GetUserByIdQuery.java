package com.tickon.identity.user.application.query.getuserbyid;

import com.tickon.common.queries.Query;
import com.tickon.identity.user.application.UserResult;
import com.tickon.identity.user.domain.valueobjects.UserId;
import java.util.Optional;

public record GetUserByIdQuery(UserId userId) implements Query<Optional<UserResult>> {}
