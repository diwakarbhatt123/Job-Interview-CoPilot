package com.jobcopilot.account_service.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EnableConfigurationProperties
@Configuration
@ConfigurationProperties(prefix = "auth.jwt")
public class TokenConfig {
  private String publicKeyPath;
  private String issuer;
  private long expirationSeconds;
  private String privateKeyPath;
  private String privateKeyAlias;
  private String privateKeyPassword;
}
