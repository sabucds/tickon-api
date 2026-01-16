package com.tickon.gateway.config;

import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Flux;

@Configuration
public class JwtDecoderConfig {

  private static final Pattern PEM_PATTERN = Pattern.compile("-----BEGIN (.*)-----|-----END (.*)-----|\\s");

  @Bean
  ReactiveJwtDecoder reactiveJwtDecoder(@Value("${security.jwt.public-key:}") String publicKeyPem) {
    if (publicKeyPem == null || publicKeyPem.isBlank()) {
      throw new IllegalStateException("Missing security.jwt.public-key (SECURITY_JWT_PUBLIC_KEY)");
    }

    ECPublicKey publicKey = parsePublicKey(publicKeyPem);
    Curve curve = Curve.forECParameterSpec(publicKey.getParams());
    JWK jwk = new ECKey.Builder(curve, publicKey).build();

    return NimbusReactiveJwtDecoder.withJwkSource(signedJwt -> Flux.just(jwk)).jwsAlgorithm(SignatureAlgorithm.ES256)
        .build();
  }

  private ECPublicKey parsePublicKey(String pem) {
    try {
      byte[] keyBytes = decodePem(pem);
      X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
      return (ECPublicKey) KeyFactory.getInstance("EC").generatePublic(keySpec);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to parse EC public key", e);
    }
  }

  private byte[] decodePem(String pem) {
    String sanitized = PEM_PATTERN.matcher(pem).replaceAll("");
    return Base64.getDecoder().decode(sanitized);
  }
}
