package com.tickon.identity.auth.application.query.verifyresettoken;

import com.tickon.common.queries.Query;

public record VerifyResetTokenQuery(String resetToken) implements Query<VerifyResetTokenResult> {}
