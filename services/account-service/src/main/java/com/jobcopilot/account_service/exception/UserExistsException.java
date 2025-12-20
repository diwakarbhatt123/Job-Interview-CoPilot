package com.jobcopilot.account_service.exception;

public class UserExistsException extends RuntimeException {
  private static final String message = "User with given email %s already exists.";

  public UserExistsException(String email) {
    super(String.format(message, email));
  }
}
