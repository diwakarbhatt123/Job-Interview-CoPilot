package com.jobcopilot.account_service.controller;

import com.jobcopilot.account_service.config.TokenConfig;
import com.jobcopilot.account_service.dto.AuthenticationResult;
import com.jobcopilot.account_service.exception.BadCredentialsException;
import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.model.response.ErrorResponse;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.service.UserLoginService;
import com.jobcopilot.account_service.service.UserRegistrationService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.jobcopilot.auth.exception.InvalidTokenException;
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
  private static final String USER_ID_HEADER = "X-User-Id";
  private static final String AUTH_TOKEN_COOKIE_NAME = "AuthToken";

  private final UserRegistrationService userRegistrationService;
  private final UserLoginService userLoginService;
  private final TokenConfig tokenConfig;

  @Autowired
  public AuthController(
      UserRegistrationService userRegistrationService,
      UserLoginService userLoginService,
      TokenConfig tokenConfig) {
    this.userRegistrationService = userRegistrationService;
    this.userLoginService = userLoginService;
    this.tokenConfig = tokenConfig;
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
      @RequestBody @Valid UserLoginRequest userLoginRequest, HttpServletResponse response) {
    log.info("Received request to login user with email: {}", userLoginRequest.email());
    AuthenticationResult authenticationResult = userLoginService.authenticateUser(userLoginRequest);

    Cookie cookie = new Cookie(AUTH_TOKEN_COOKIE_NAME, authenticationResult.token());
    cookie.setHttpOnly(true);
    cookie.setPath("/");
    cookie.setMaxAge(Long.valueOf(tokenConfig.expirationSeconds()).intValue());
    response.addCookie(cookie);

    LoginResponse loginResponse = new LoginResponse(authenticationResult.userId().toString());

    return ResponseEntity.ok(loginResponse);
  }

  @GetMapping(path = "/authenticate")
  public ResponseEntity<Void> authenticateUser(
      @RequestHeader(name = "Authorization", required = false) String authenticationToken) {
    log.info("Received request to authenticate user");
    if (authenticationToken == null || authenticationToken.isBlank()) {
      throw new BadCredentialsException("Missing or empty Authorization header");
    }
    authenticationToken = authenticationToken.trim().replaceFirst("^Bearer ", "");
    String userId = userLoginService.authenticateUserToken(authenticationToken);
    log.info("Authenticated user {}", userId);
    return ResponseEntity.ok().header(USER_ID_HEADER, userId).build();
  }

  @ExceptionHandler(exception = {UserExistsException.class, DataIntegrityViolationException.class})
  public ResponseEntity<ErrorResponse> handleUserExistsException(Exception ex) {
    log.error("User registration failed: User already exists", ex);
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ErrorResponse("User already exists"));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleInvalidRequest(MethodArgumentNotValidException ex) {
    log.error("Invalid registration request: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(ex.getMessage()));
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException ex, HttpServletRequest request, HttpServletResponse response) {
    log.error("Authentication failed: {}", ex.getMessage(), ex);

    if (request.getCookies() != null) {
      Arrays.stream(request.getCookies())
          .filter(cookie -> cookie.getName().equals(AUTH_TOKEN_COOKIE_NAME))
          .findFirst()
          .ifPresent(
              cookie -> {
                cookie.setMaxAge(0);
                response.addCookie(cookie);
              });
    }

    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid email or password"));
  }

  @ExceptionHandler(InvalidTokenException.class)
  public ResponseEntity<ErrorResponse> handleInvalidTokenException(InvalidTokenException ex) {
    log.error("Authentication token validation failed: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(new ErrorResponse("Invalid authentication token"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Internal server error: {}", ex.getMessage(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(new ErrorResponse("An unexpected error occurred."));
  }
}
