package org.jobcopilot.auth.config.generator;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JWTTokenGeneratorConfig extends TokenGeneratorConfig {
  private final String privateKeyPath;
  private final String privateKeyAlias;
  private final String privateKeyPassword;
  private final String issuer;
  private final long expirationSeconds;
}
