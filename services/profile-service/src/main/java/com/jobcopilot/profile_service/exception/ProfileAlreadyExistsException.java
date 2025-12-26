package com.jobcopilot.profile_service.exception;

public class ProfileAlreadyExistsException extends RuntimeException {

  public ProfileAlreadyExistsException(String displayName) {
    String message = "Profile with display name %s already exists for the given user ID.";
    super(String.format(message, displayName));
  }
}
