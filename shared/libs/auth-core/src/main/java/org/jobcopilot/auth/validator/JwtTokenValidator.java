package org.jobcopilot.auth.validator;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.regex.Pattern;
import lombok.Data;
import org.jobcopilot.auth.exception.InvalidTokenException;
import org.jobcopilot.auth.model.ValidatedToken;

@Data
public class JwtTokenValidator implements TokenValidator {
  private final PublicKey publicKey;
  private final String issuer;

  private static final Pattern RS256_ALG_PATTERN = Pattern.compile("\"alg\"\\s*:\\s*\"RS256\"");

  @Override
  public ValidatedToken validateAndDecodeToken(String token) throws InvalidTokenException {
    requireRs256Header(token);

    try {
      var jwt =
          Jwts.parser()
              .verifyWith(publicKey)
              .requireIssuer(issuer)
              .build()
              .parseSignedClaims(token);

      var claims = jwt.getPayload();

      return new ValidatedToken(
          claims.getSubject(),
          claims.getIssuer(),
          toInstant(claims.getIssuedAt()),
          toInstant(claims.getNotBefore()),
          toInstant(claims.getExpiration()),
          Map.copyOf(claims));
    } catch (JwtException e) {
      throw new InvalidTokenException("Invalid JWT token", e);
    }
  }

  private Instant toInstant(Date date) {
    return date == null ? null : date.toInstant();
  }

  private void requireRs256Header(String token) {
    String[] parts = token == null ? new String[0] : token.split("\\.");
    if (parts.length != 3) {
      throw new JwtException("Invalid JWT format");
    }

    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
    if (!RS256_ALG_PATTERN.matcher(headerJson).find()) {
      throw new JwtException("Unsupported JWT alg (expected RS256)");
    }
  }
}
