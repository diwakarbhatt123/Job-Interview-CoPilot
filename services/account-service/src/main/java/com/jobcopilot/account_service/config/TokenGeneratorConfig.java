package com.jobcopilot.account_service.config;

import java.io.IOException;
import java.security.GeneralSecurityException;
import org.jobcopilot.auth.config.generator.JWTTokenGeneratorConfig;
import org.jobcopilot.auth.factory.TokenGeneratorFactory;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TokenGeneratorConfig {

  @Value("${auth.jwt.privateKeyPath}")
  private String privateKeyPath;

  @Value("${auth.jwt.privateKeyAlias}")
  private String privateKeyAlias;

  @Value("${auth.jwt.privateKeyPassword}")
  private String privateKeyPassword;

  @Value("${auth.jwt.issuer}")
  private String issuer;

  @Value("${auth.jwt.expirationSeconds}")
  private long expirationSeconds;

  @Bean
  public TokenGenerator getTokenGenerator() throws GeneralSecurityException, IOException {
    JWTTokenGeneratorConfig config =
        new JWTTokenGeneratorConfig(
            privateKeyPath, privateKeyAlias, privateKeyPassword, issuer, expirationSeconds);
    return TokenGeneratorFactory.createTokenGenerator(config);
  }
}
