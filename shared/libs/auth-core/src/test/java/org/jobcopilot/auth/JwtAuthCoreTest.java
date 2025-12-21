package org.jobcopilot.auth;

import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import org.jobcopilot.auth.exception.InvalidTokenException;
import org.jobcopilot.auth.generator.JWTTokenGenerator;
import org.jobcopilot.auth.model.ValidatedToken;
import org.jobcopilot.auth.validator.JwtTokenValidator;
import org.junit.jupiter.api.Test;

class JwtAuthCoreTest {

  private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }

  @Test
  void shouldGenerateAndValidateJwt() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 300);
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    String token = generator.generateToken("user-123");
    ValidatedToken result = validator.validateAndDecodeToken(token);

    assertEquals("user-123", result.subject());
    assertEquals("issuer-a", result.issuer());
    assertNotNull(result.expiresAt());
    assertTrue(result.expiresAt().isAfter(Instant.now().minusSeconds(1)));
  }

  @Test
  void shouldRejectTokenWithWrongIssuer() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 300);
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-b");

    String token = generator.generateToken("user-123");

    assertThrows(InvalidTokenException.class, () -> validator.validateAndDecodeToken(token));
  }

  @Test
  void shouldRejectExpiredToken() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    // Negative expiration to force immediate expiry
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", -5);
    var validator = new JwtTokenValidator(keyPair.getPublic(), "issuer-a");

    String token = generator.generateToken("user-123");

    assertThrows(InvalidTokenException.class, () -> validator.validateAndDecodeToken(token));
  }
}
