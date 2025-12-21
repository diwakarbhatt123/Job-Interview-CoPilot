package com.jobcopilot.account_service.controller;

import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.model.response.ErrorResponse;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.service.UserLoginService;
import com.jobcopilot.account_service.service.UserRegistrationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.jobcopilot.auth.exception.InvalidTokenException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {
  private static final String USER_ID_HEADER = "X-User-Id";

  private final UserRegistrationService userRegistrationService;
  private final UserLoginService userLoginService;

  @Autowired
  public AuthController(
      UserRegistrationService userRegistrationService, UserLoginService userLoginService) {
    this.userRegistrationService = userRegistrationService;
    this.userLoginService = userLoginService;
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

  @PostMapping(
      path = "/login",
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<LoginResponse> loginUser(
      @RequestBody @Valid UserLoginRequest userLoginRequest) {
    log.info("Received request to login user with email: {}", userLoginRequest.email());
    LoginResponse loginResponse = userLoginService.authenticateUser(userLoginRequest);
    return ResponseEntity.ok(loginResponse);
  }

  @GetMapping(path = "/authenticate")
  public ResponseEntity<Void> authenticateUser(
      @RequestHeader("Authorization") @NotBlank String authenticationToken) {
    log.info("Received request to authenticate user");
    authenticationToken = authenticationToken.trim().replaceFirst("^Bearer ", "");
    String userId = userLoginService.authenticateUserToken(authenticationToken);
    return ResponseEntity.ok().header(USER_ID_HEADER, userId).build();
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

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
    log.error("Authentication failed: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid email or password"));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
    log.error("Authentication token validation failed: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid authentication token"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred."));
  }
}
