package com.jobcopilot.account_service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.jobcopilot.account_service.entity.User;
import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class UserRegistrationServiceTest {

  @Test
  void registerUser_hashesPassword_andSavesUser() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    UserRegistrationService service = new UserRegistrationService(userRepository, passwordEncoder);

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());
    when(passwordEncoder.encode("password123")).thenReturn("hashed");

    service.registerUser(new UserRegistrationRequest("User@Example.com", "password123"));

    ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
    verify(userRepository).save(captor.capture());
    User saved = captor.getValue();

    assertEquals("user@example.com", saved.getEmail());
    assertEquals("hashed", saved.getPasswordHash());
    assertTrue(saved.isActive());
  }

  @Test
  void registerUser_throwsUserExists_whenEmailAlreadyPresent() {
    UserRepository userRepository = mock(UserRepository.class);
    PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    UserRegistrationService service = new UserRegistrationService(userRepository, passwordEncoder);

    when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(new User()));

    assertThrows(
        UserExistsException.class,
        () -> service.registerUser(new UserRegistrationRequest("user@example.com", "password123")));
    verify(userRepository, never()).save(any());
  }
}
