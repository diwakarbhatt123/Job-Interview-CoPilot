package com.jobcopilot.account_service.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jobcopilot.auth.config.validator.JWTTokenValidatorConfig;
import org.jobcopilot.auth.factory.TokenValidatorFactory;
import org.jobcopilot.auth.validator.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenValidatorConfig {

  private final TokenConfig tokenConfig;

  @Autowired
  public TokenValidatorConfig(TokenConfig tokenConfig) {
    this.tokenConfig = tokenConfig;
  }

  @Bean
  public TokenValidator tokenValidator() throws GeneralSecurityException, IOException {
    JWTTokenValidatorConfig tokenValidatorConfig =
        new JWTTokenValidatorConfig(tokenConfig.getPublicKeyPath(), tokenConfig.getIssuer());
    return TokenValidatorFactory.createTokenValidator(tokenValidatorConfig);
  }
}
