package org.jobcopilot.auth.factory;

import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jobcopilot.auth.config.validator.JWTTokenValidatorConfig;
import org.jobcopilot.auth.config.validator.TokenValidatorConfig;
import org.jobcopilot.auth.utils.KeyUtils;
import org.jobcopilot.auth.validator.JwtTokenValidator;
import org.jobcopilot.auth.validator.TokenValidator;

public class TokenValidatorFactory {

  public static TokenValidator createTokenValidator(TokenValidatorConfig config)
      throws IOException, GeneralSecurityException {
    if (requireNonNull(config) instanceof JWTTokenValidatorConfig jwtConfig) {
      validate(jwtConfig);
      return new JwtTokenValidator(
          KeyUtils.getPublicKeyFromPem(jwtConfig.getPublicKeyPath()), jwtConfig.getIssuer());
    }
    throw new IllegalArgumentException(
        "Unsupported TokenValidatorConfig type: " + config.getClass().getName());
  }

  private static void validate(JWTTokenValidatorConfig config) {
    if (isNull(config.getPublicKeyPath()) || config.getPublicKeyPath().isBlank()) {
      throw new IllegalArgumentException("publicKeyPath must be set");
    }
    if (isNull(config.getIssuer()) || config.getIssuer().isBlank()) {
      throw new IllegalArgumentException("issuer must be set");
    }
  }
}
