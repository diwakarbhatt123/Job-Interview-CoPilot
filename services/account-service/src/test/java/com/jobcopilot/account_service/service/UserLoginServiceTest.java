package com.jobcopilot.account_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jobcopilot.account_service.entity.User;
import com.jobcopilot.account_service.exception.BadCredentialsException;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.jobcopilot.auth.model.ValidatedToken;
import org.jobcopilot.auth.validator.TokenValidator;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserLoginServiceTest {

  @Test
  void authenticateUser_returnsToken_whenCredentialsValid() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    UUID userId = UUID.randomUUID();
    User user = new User();
    user.setId(42L);
    user.setUserId(userId);
    user.setPasswordHash("hashed");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
    when(tokenGenerator.generateToken(userId.toString())).thenReturn("jwt-token");

    LoginResponse response =
        service.authenticateUser(new UserLoginRequest("USER@EXAMPLE.COM", "password123"));

    assertEquals("jwt-token", response.token());
    assertEquals(userId.toString(), response.userId());
  }

  @Test
  void authenticateUser_throwsBadCredentials_whenUserMissing() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

    assertThrows(
        BadCredentialsException.class,
        () -> service.authenticateUser(new UserLoginRequest("user@example.com", "password123")));
  }

  @Test
  void authenticateUser_throwsBadCredentials_whenPasswordInvalid() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    User user = new User();
    user.setId(42L);
    user.setPasswordHash("hashed");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashed")).thenReturn(false);

    assertThrows(
        BadCredentialsException.class,
        () -> service.authenticateUser(new UserLoginRequest("user@example.com", "password123")));
  }

  @Test
  void authenticateUserToken_returnsSubject_whenValid() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    UUID userId = UUID.randomUUID();
    ValidatedToken token =
        new ValidatedToken(
            userId.toString(),
            "account-service",
            Instant.now(),
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of());

    when(tokenValidator.validateAndDecodeToken("token")).thenReturn(token);
    when(userRepository.existsByUserId(userId)).thenReturn(true);

    String subject = service.authenticateUserToken("token");
    assertEquals(userId.toString(), subject);
  }

  @Test
  void authenticateUserToken_throwsBadCredentials_whenUserMissing() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    UUID userId = UUID.randomUUID();
    ValidatedToken token =
        new ValidatedToken(
            userId.toString(),
            "account-service",
            Instant.now(),
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of());

    when(tokenValidator.validateAndDecodeToken("token")).thenReturn(token);
    when(userRepository.existsByUserId(userId)).thenReturn(false);

    assertThrows(BadCredentialsException.class, () -> service.authenticateUserToken("token"));
  }

  @Test
  void authenticateUserToken_throwsBadCredentials_whenExpired() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    UUID userId = UUID.randomUUID();
    ValidatedToken token =
        new ValidatedToken(
            userId.toString(),
            "account-service",
            Instant.now(),
            Instant.now(),
            Instant.now().minusSeconds(5),
            java.util.Map.of());

    when(tokenValidator.validateAndDecodeToken("token")).thenReturn(token);

    assertThrows(BadCredentialsException.class, () -> service.authenticateUserToken("token"));
  }

  @Test
  void authenticateUserToken_throwsBadCredentials_whenIssuerMismatch() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);
    TokenValidator tokenValidator = mock(TokenValidator.class);

    UserLoginService service =
        new UserLoginService(
            userRepository, passwordEncoder, tokenGenerator, tokenValidator, "account-service");

    UUID userId = UUID.randomUUID();
    ValidatedToken token =
        new ValidatedToken(
            userId.toString(),
            "other-issuer",
            Instant.now(),
            Instant.now(),
            Instant.now().plusSeconds(3600),
            java.util.Map.of());

    when(tokenValidator.validateAndDecodeToken("token")).thenReturn(token);

    assertThrows(BadCredentialsException.class, () -> service.authenticateUserToken("token"));
  }
}
