package com.tickon.identity.auth.infrastructure.security;

import com.tickon.identity.auth.application.ports.out.ResetTokenHasher;
import com.tickon.identity.auth.domain.valueobjects.ResetTokenHash;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MacResetTokenHasher implements ResetTokenHasher {

  private static final String HMAC_ALG = "HmacSHA256";
  private final byte[] pepperBytes;

  public MacResetTokenHasher(@Value("${security.password-reset.token-pepper:changeme-dev-only}") String pepper) {
    if (pepper == null || pepper.isBlank()) {
      throw new IllegalStateException("Missing security.password-reset.token-pepper configuration");
    }
    this.pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);
  }

  @Override
  public ResetTokenHash hash(String plainToken) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(pepperBytes, HMAC_ALG));
      byte[] digest = mac.doFinal(plainToken.getBytes(StandardCharsets.UTF_8));
      return ResetTokenHash.from(Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compute reset token HMAC", e);
    }
  }
}
