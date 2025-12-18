package org.jobcopilot.auth.model;

import java.time.Instant;
import java.util.Map;

public record ValidatedToken(
    String subject,
    String issuer,
    Instant issuedAt,
    Instant notBefore,
    Instant expiresAt,
    Map<String, Object> claims) {

  public ValidatedToken {
    claims = claims == null ? Map.of() : Map.copyOf(claims);
  }

  @Override
  public Map<String, Object> claims() {
    return Map.copyOf(claims);
  }
}
