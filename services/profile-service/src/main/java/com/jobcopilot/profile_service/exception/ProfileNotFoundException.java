package com.jobcopilot.profile_service.exception;

public class ProfileNotFoundException extends RuntimeException {
  private static final String ERROR_MESSAGE = "Profile with id %s not found";

  public ProfileNotFoundException(String profileId) {
    super(String.format(ERROR_MESSAGE, profileId));
  }
}
