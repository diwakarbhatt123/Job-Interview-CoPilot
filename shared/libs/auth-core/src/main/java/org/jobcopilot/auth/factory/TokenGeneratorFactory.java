package org.jobcopilot.auth.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jobcopilot.auth.config.generator.JWTTokenGeneratorConfig;
import org.jobcopilot.auth.config.generator.TokenGeneratorConfig;
import org.jobcopilot.auth.generator.JWTTokenGenerator;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.jobcopilot.auth.utils.KeyUtils;

public class TokenGeneratorFactory {

  public static TokenGenerator createTokenGenerator(TokenGeneratorConfig config)
      throws IOException, GeneralSecurityException {
    if (requireNonNull(config) instanceof JWTTokenGeneratorConfig jwtConfig) {
      validate(jwtConfig);

      return new JWTTokenGenerator(
          KeyUtils.getPrivateKeyFromPkcs12(
              jwtConfig.getPrivateKeyPath(),
              jwtConfig.getPrivateKeyAlias(),
              jwtConfig.getPrivateKeyPassword().toCharArray()),
          jwtConfig.getIssuer(),
          jwtConfig.getExpirationSeconds());
    }
    throw new IllegalArgumentException(
        "Unsupported TokenGeneratorConfig type: " + config.getClass().getName());
  }

  private static void validate(JWTTokenGeneratorConfig config) {
    if (isNull(config.getPrivateKeyPath()) || config.getPrivateKeyPath().isBlank()) {
      throw new IllegalArgumentException("privateKeyPath must be set");
    }
    if (isNull(config.getPrivateKeyAlias()) || config.getPrivateKeyAlias().isBlank()) {
      throw new IllegalArgumentException("privateKeyAlias must be set");
    }
    if (isNull(config.getPrivateKeyPassword()) || config.getPrivateKeyPassword().isBlank()) {
      throw new IllegalArgumentException("privateKeyPassword must be set");
    }
    if (isNull(config.getIssuer()) || config.getIssuer().isBlank()) {
      throw new IllegalArgumentException("issuer must be set");
    }
    if (config.getExpirationSeconds() <= 0) {
      throw new IllegalArgumentException("expirationSeconds must be > 0");
    }
  }
}
