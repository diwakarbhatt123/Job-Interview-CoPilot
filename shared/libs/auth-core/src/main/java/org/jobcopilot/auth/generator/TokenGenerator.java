package org.jobcopilot.auth.generator;

public interface TokenGenerator {
  String generateToken(String userId);
}
