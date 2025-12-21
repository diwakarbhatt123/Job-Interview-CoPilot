package org.jobcopilot.auth.validator;

import org.jobcopilot.auth.exception.InvalidTokenException;
import org.jobcopilot.auth.model.ValidatedToken;

public interface TokenValidator {
  ValidatedToken validateAndDecodeToken(String token) throws InvalidTokenException;
}
