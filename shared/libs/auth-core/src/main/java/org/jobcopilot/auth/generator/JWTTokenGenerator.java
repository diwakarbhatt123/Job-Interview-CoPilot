package org.jobcopilot.auth.generator;

import static java.util.Objects.isNull;

import io.jsonwebtoken.Jwts;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;
import lombok.Data;

@Data
public class JWTTokenGenerator implements TokenGenerator {

  private final PrivateKey privateKey;
  private final String issuer;
  private final long expirationSeconds;

  @Override
  public String generateToken(String userId) {
    if (isNull(userId) || userId.isBlank()) {
      throw new IllegalArgumentException("userId must be set");
    }

    Instant now = Instant.now();
    Instant expiry = now.plusSeconds(expirationSeconds);

    return Jwts.builder()
        .subject(userId)
        .issuer(issuer)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .notBefore(Date.from(now))
        .signWith(privateKey, Jwts.SIG.RS256)
        .compact();
  }
}
