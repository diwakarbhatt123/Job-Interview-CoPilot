package com.jobcopilot.account_service.controller;

import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.model.response.ErrorResponse;
import com.jobcopilot.account_service.service.UserRegistrationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

  private final UserRegistrationService userRegistrationService;

  @Autowired
  public AuthController(UserRegistrationService userRegistrationService) {
    this.userRegistrationService = userRegistrationService;
  }

  @PostMapping(
      path = "/register",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> registerUser(
      @RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
    log.info("Received request to register user with email: {}", userRegistrationRequest.email());
    userRegistrationService.registerUser(userRegistrationRequest);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @ExceptionHandler(exception = {UserExistsException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ErrorResponse> handleUserExistsException() {
    log.error("User registration failed: User already exists");
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("User already exists"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex) {
    log.error("Invalid registration request: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred."));
  }
}
