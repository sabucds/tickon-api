package com.tickon.identity.auth.infrastructure.security;

import com.tickon.identity.auth.application.ports.out.RefreshTokenHasher;
import com.tickon.identity.auth.domain.valueobjects.RefreshTokenHash;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MacRefreshTokenHasher implements RefreshTokenHasher {

  private static final String HMAC_ALG = "HmacSHA256";

  private final byte[] pepperBytes;

  public MacRefreshTokenHasher(@Value("${security.jwt.refresh-token-pepper}") String pepper) {
    if (pepper == null || pepper.isBlank()) {
      throw new IllegalStateException("Missing security.jwt.refresh-token-pepper");
    }
    this.pepperBytes = pepper.getBytes(StandardCharsets.UTF_8);
  }

  public RefreshTokenHash hash(String refreshToken) {
    try {
      Mac mac = Mac.getInstance(HMAC_ALG);
      mac.init(new SecretKeySpec(pepperBytes, HMAC_ALG));
      byte[] digest = mac.doFinal(refreshToken.getBytes(StandardCharsets.UTF_8));
      return RefreshTokenHash.from(Base64.getUrlEncoder().withoutPadding().encodeToString(digest));
    } catch (Exception e) {
      throw new IllegalStateException("Unable to compute refresh token HMAC", e);
    }
  }

  public boolean equalsConstantTime(String a, String b) {
    if (a == null || b == null)
      return false;
    return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
  }
}
