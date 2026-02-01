package com.jobcopilot.job_analyzer_service.exception;

public class MissingUserIdException extends RuntimeException {
  public MissingUserIdException() {
    super("Missing or invalid X-User-Id header");
  }
}
