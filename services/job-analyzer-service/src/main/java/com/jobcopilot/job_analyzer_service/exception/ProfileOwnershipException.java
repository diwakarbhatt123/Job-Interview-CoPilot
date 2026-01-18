package com.jobcopilot.job_analyzer_service.exception;

public class ProfileOwnershipException extends RuntimeException {
  public ProfileOwnershipException(String profileId) {
    super("Profile does not belong to the authenticated user: " + profileId);
  }
}
