package com.jobcopilot.account_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.jobcopilot.account_service.entity.User;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.repository.UserRepository;
import java.util.Optional;
import java.util.UUID;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserLoginServiceTest {

  @Test
  void authenticateUser_returnsToken_whenCredentialsValid() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    TokenGenerator tokenGenerator = mock(TokenGenerator.class);

    UserLoginService service =
        new UserLoginService(userRepository, passwordEncoder, tokenGenerator);

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

    UserLoginService service =
        new UserLoginService(userRepository, passwordEncoder, tokenGenerator);

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

    UserLoginService service =
        new UserLoginService(userRepository, passwordEncoder, tokenGenerator);

    User user = new User();
    user.setId(42L);
    user.setPasswordHash("hashed");

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("password123", "hashed")).thenReturn(false);

    assertThrows(
        BadCredentialsException.class,
        () -> service.authenticateUser(new UserLoginRequest("user@example.com", "password123")));
  }
}
