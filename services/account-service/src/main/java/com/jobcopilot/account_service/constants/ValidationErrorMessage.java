package com.jobcopilot.account_service.constants;

public class ValidationErrorMessage {
  public static final String INVALID_EMAIL = "Invalid email format.";
  public static final String INVALID_PASSWORD_LENGTH =
      "Password must be between 8 and 64 characters long.";

  private ValidationErrorMessage() {}
}
