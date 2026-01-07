package com.tickon.identity.auth.infrastructure.jwt;

import com.tickon.identity.auth.application.ports.out.TokenProvider;
import com.tickon.identity.user.domain.User;
import io.jsonwebtoken.Jwts;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Date;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider implements TokenProvider {

  private final PrivateKey signingKey;
  private final PublicKey verificationKey;
  private final long accessTokenValidityMs;
  private final String issuer;
  private static final SecureRandom SECURE_RANDOM = new SecureRandom();

  public JwtTokenProvider(@Value("${security.jwt.private-key:}") String privateKeyPem,
      @Value("${security.jwt.public-key:}") String publicKeyPem,
      @Value("${security.jwt.access-token-expiration}") long accessTokenValidityMs,
      @Value("${security.jwt.issuer:tickon}") String issuer) {
    KeyPair keyPair = resolveKeyPair(privateKeyPem, publicKeyPem);
    this.signingKey = keyPair.getPrivate();
    this.verificationKey = keyPair.getPublic();
    this.accessTokenValidityMs = accessTokenValidityMs;
    this.issuer = issuer;
  }

  @Override
  public String generateAccessToken(User user) {
    return generateToken(user, accessTokenValidityMs);
  }

  @Override
  public String generateRefreshToken(User user) {
    return generateSecureRandomToken();
  }

  private String generateToken(User user, long validityMs) {
    Instant now = Instant.now();
    return Jwts.builder().subject(user.id().value().toString()).issuer(issuer).issuedAt(Date.from(now))
        .expiration(Date.from(now.plusMillis(validityMs))).claim("username", user.username().value())
        .claim("email", user.email().value()).signWith(signingKey, Jwts.SIG.ES256).compact();
  }

  private String generateSecureRandomToken() {
    byte[] randomBytes = new byte[32];
    SECURE_RANDOM.nextBytes(randomBytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
  }

  private KeyPair resolveKeyPair(String privateKeyPem, String publicKeyPem) {
    try {
      boolean hasProvidedKeys = !privateKeyPem.isBlank() && !publicKeyPem.isBlank();
      if (hasProvidedKeys) {
        PrivateKey privateKey = parsePrivateKey(privateKeyPem);
        PublicKey publicKey = parsePublicKey(publicKeyPem);
        return new KeyPair(publicKey, privateKey);
      }

      KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
      generator.initialize(256);
      return generator.generateKeyPair();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to initialize JWT key pair", e);
    }
  }

  private PrivateKey parsePrivateKey(String pem) throws Exception {
    byte[] keyBytes = decodePem(pem);
    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("EC").generatePrivate(keySpec);
  }

  private PublicKey parsePublicKey(String pem) throws Exception {
    byte[] keyBytes = decodePem(pem);
    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
    return KeyFactory.getInstance("EC").generatePublic(keySpec);
  }

  private byte[] decodePem(String pem) {
    String sanitized = Pattern.compile("-----BEGIN (.*)-----|-----END (.*)-----|\\s").matcher(pem).replaceAll("");
    return java.util.Base64.getDecoder().decode(sanitized);
  }
}
