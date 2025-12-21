package com.jobcopilot.account_service.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jobcopilot.auth.config.generator.JWTTokenGeneratorConfig;
import org.jobcopilot.auth.factory.TokenGeneratorFactory;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenGeneratorConfig {

  private final TokenConfig tokenConfig;

  @Autowired
  public TokenGeneratorConfig(TokenConfig tokenConfig) {
    this.tokenConfig = tokenConfig;
  }

  @Bean
  public TokenGenerator getTokenGenerator() throws GeneralSecurityException, IOException {
    JWTTokenGeneratorConfig config =
        new JWTTokenGeneratorConfig(
            tokenConfig.getPrivateKeyPath(),
            tokenConfig.getPrivateKeyAlias(),
            tokenConfig.getPrivateKeyPassword(),
            tokenConfig.getIssuer(),
            tokenConfig.getExpirationSeconds());
    return TokenGeneratorFactory.createTokenGenerator(config);
  }
}
