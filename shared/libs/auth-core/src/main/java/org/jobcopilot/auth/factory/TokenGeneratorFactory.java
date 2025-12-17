package org.jobcopilot.auth.factory;

import org.jobcopilot.auth.config.generator.JWTTokenGeneratorConfig;
import org.jobcopilot.auth.config.generator.TokenGeneratorConfig;
import org.jobcopilot.auth.generator.JWTTokenGenerator;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.jobcopilot.auth.utils.KeyUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Objects.requireNonNull;

public class TokenGeneratorFactory {

  public static TokenGenerator createTokenGenerator(TokenGeneratorConfig config)
      throws IOException, GeneralSecurityException {
    if (requireNonNull(config) instanceof JWTTokenGeneratorConfig jwtConfig) {

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
}
