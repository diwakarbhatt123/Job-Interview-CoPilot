package com.jobcopilot.job_analyzer_service.controller;

import com.jobcopilot.job_analyzer_service.exception.ProfileOwnershipException;
import com.jobcopilot.job_analyzer_service.exception.ProfileServiceException;
import com.jobcopilot.job_analyzer_service.model.request.SubmitJobAnalysisRequest;
import com.jobcopilot.job_analyzer_service.model.response.ErrorResponse;
import com.jobcopilot.job_analyzer_service.model.response.JobAnalysisResponse;
import com.jobcopilot.job_analyzer_service.service.JobAnalysisService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/job/analysis")
public class JobAnalysisController {

  private final JobAnalysisService jobAnalysisService;

  public JobAnalysisController(JobAnalysisService jobAnalysisService) {
    this.jobAnalysisService = jobAnalysisService;
  }

  @PostMapping("/submit")
  public ResponseEntity<JobAnalysisResponse> submitJobAnalysis(
      @Valid @RequestBody SubmitJobAnalysisRequest request,
      @RequestHeader("X-User-Id") String userId) {
    if (userId == null || userId.isBlank()) {
      throw new MissingUserIdException();
    }
    return ResponseEntity.accepted().body(jobAnalysisService.submitJobAnalysis(request, userId));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
      MethodArgumentNotValidException ex) {
    log.error("Validation failed: {}", ex.getMessage(), ex);
    String errorMessage = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
    ErrorResponse errorResponse = new ErrorResponse(errorMessage);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
  }

  @ExceptionHandler(ProfileOwnershipException.class)
  public ResponseEntity<ErrorResponse> handleProfileOwnershipException(
      ProfileOwnershipException ex) {
    log.error("Profile ownership validation failed: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(ProfileServiceException.class)
  public ResponseEntity<ErrorResponse> handleProfileServiceException(ProfileServiceException ex) {
    log.error("Profile service call failed: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(new ErrorResponse("Profile service unavailable."));
  }

  @ExceptionHandler(MissingUserIdException.class)
  public ResponseEntity<ErrorResponse> handleMissingUserIdException(MissingUserIdException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("An unexpected error occurred: {}", ex.getMessage(), ex);
    ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred.");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
  }
}
