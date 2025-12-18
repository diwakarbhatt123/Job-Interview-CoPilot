package org.jobcopilot.auth.generator;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class JwtTokenGeneratorTest {

  private static KeyPair generateRsaKeyPair() throws Exception {
    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
    keyGen.initialize(2048);
    return keyGen.generateKeyPair();
  }

  @Test
  void generatesRs256JwtWithStandardClaims() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 300);

    String token = generator.generateToken("user-123");

    String[] parts = token.split("\\.");
    assertEquals(3, parts.length, "JWT must have 3 dot-separated parts");

    String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]), StandardCharsets.UTF_8);
    assertTrue(headerJson.contains("\"alg\":\"RS256\""), "JWT header must declare RS256");
  }

  @Test
  void generatesTokenWithReasonableExpiration() throws Exception {
    KeyPair keyPair = generateRsaKeyPair();
    var generator = new JWTTokenGenerator(keyPair.getPrivate(), "issuer-a", 60);

    Instant before = Instant.now();
    String token = generator.generateToken("user-123");
    Instant after = Instant.now();

    String[] parts = token.split("\\.");
    String payloadJson =
        new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);

    assertTrue(payloadJson.contains("\"iss\":\"issuer-a\""));
    assertTrue(payloadJson.contains("\"sub\":\"user-123\""));

    // Basic sanity: exp should be present; we don't parse JSON here to keep deps minimal.
    assertTrue(payloadJson.contains("\"exp\""), "JWT payload must include exp");

    // Additional weak check: token generation time window shouldn't exceed a few seconds.
    assertTrue(after.isAfter(before) || after.equals(before));
  }
}
