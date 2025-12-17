package org.jobcopilot.auth.generator;

import io.jsonwebtoken.Jwts;
import lombok.Data;

import java.security.PrivateKey;
import java.time.Instant;
import java.util.Date;

@Data
public class JWTTokenGenerator implements TokenGenerator {

    private final PrivateKey privateKey;
    private final String issuer;
    private final long expirationSeconds;

    @Override
    public String generateToken(String userId) {
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
