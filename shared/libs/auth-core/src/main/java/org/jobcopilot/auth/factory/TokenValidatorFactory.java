package org.jobcopilot.auth.factory;

import org.jobcopilot.auth.config.validator.JWTTokenValidatorConfig;
import org.jobcopilot.auth.config.validator.TokenValidatorConfig;
import org.jobcopilot.auth.utils.KeyUtils;
import org.jobcopilot.auth.validator.JwtTokenValidator;
import org.jobcopilot.auth.validator.TokenValidator;

import java.io.IOException;
import java.security.GeneralSecurityException;

import static java.util.Objects.requireNonNull;

public class TokenValidatorFactory {

  public static TokenValidator createTokenValidator(TokenValidatorConfig config)
      throws IOException, GeneralSecurityException {
    if (requireNonNull(config) instanceof JWTTokenValidatorConfig jwtConfig) {
      return new JwtTokenValidator(
          KeyUtils.getPublicKeyFromPem(jwtConfig.getPublicKeyPath()), jwtConfig.getIssuer());
    }
    throw new IllegalArgumentException("Unsupported TokenValidatorConfig type");
  }
}
