package org.jobcopilot.auth.validator;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import org.jobcopilot.auth.exception.InvalidTokenException;
import org.jobcopilot.auth.generator.JWTTokenGenerator;
import org.junit.jupiter.api.Test;

class JwtTokenValidatorTest {

  private static KeyPair generateRsaKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }

  @Test
  void validatesTokenAndReturnsStandardFields() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 300);
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    String token = generator.generateToken("user-123");
    var validated = validator.validateAndDecodeToken(token);

    assertEquals("user-123", validated.subject());
    assertEquals("issuer-a", validated.issuer());
    assertNotNull(validated.issuedAt());
    assertNotNull(validated.notBefore());
    assertNotNull(validated.expiresAt());
    assertTrue(validated.expiresAt().isAfter(Instant.now().minusSeconds(1)));
    assertTrue(validated.claims().containsKey("sub"));
    assertTrue(validated.claims().containsKey("iss"));
  }

  @Test
  void rejectsTokenSignedByDifferentKey() throws Exception {
    KeyPair signingKey = generateRsaKeyPair();
    KeyPair verifyingKey = generateRsaKeyPair();

    var generator = new JWTTokenGenerator(signingKey.getPrivate(), "issuer-a", 300);
    var validator = new JwtTokenValidator(verifyingKey.getPublic(), "issuer-a");

    String token = generator.generateToken("user-123");
    assertThrows(InvalidTokenException.class, () -> validator.validateAndDecodeToken(token));
  }

  @Test
  void rejectsTamperedTokenPayload() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 300);
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    String token = generator.generateToken("user-123");
    String[] parts = token.split("\\.");
    assertEquals(3, parts.length);

    String payloadJson =
        new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    String tamperedPayloadJson = payloadJson.replace("user-123", "user-999");
    String tamperedPayload =
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(tamperedPayloadJson.getBytes(StandardCharsets.UTF_8));
    String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

    assertThrows(
        InvalidTokenException.class, () -> validator.validateAndDecodeToken(tamperedToken));
  }

  @Test
  void rejectsTokenNotValidYet() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject("user-123")
            .issuer("issuer-a")
            .issuedAt(Date.from(now))
            .notBefore(Date.from(now.plusSeconds(60)))
            .expiration(Date.from(now.plusSeconds(120)))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

    assertThrows(InvalidTokenException.class, () -> validator.validateAndDecodeToken(token));
  }

  @Test
  void rejectsRs512EvenWhenSignatureIsValid() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject("user-123")
            .issuer("issuer-a")
            .issuedAt(Date.from(now))
            .notBefore(Date.from(now))
            .expiration(Date.from(now.plusSeconds(120)))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS512)
            .compact();

    assertThrows(InvalidTokenException.class, () -> validator.validateAndDecodeToken(token));
  }

  @Test
  void preservesCustomClaimsLikeRoles() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    Instant now = Instant.now();
    String token =
        Jwts.builder()
            .subject("user-123")
            .issuer("issuer-a")
            .issuedAt(Date.from(now))
            .notBefore(Date.from(now))
            .expiration(Date.from(now.plusSeconds(120)))
            .claim("roles", List.of("USER"))
            .signWith(keyPair.getPrivate(), Jwts.SIG.RS256)
            .compact();

    var validated = validator.validateAndDecodeToken(token);
    assertEquals(List.of("USER"), validated.claims().get("roles"));
  }
}
