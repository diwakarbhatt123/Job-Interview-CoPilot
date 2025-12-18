package org.jobcopilot.auth.config.validator;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class JWTTokenValidatorConfig extends TokenValidatorConfig {
  private final String publicKeyPath;
  private final String issuer;
}
