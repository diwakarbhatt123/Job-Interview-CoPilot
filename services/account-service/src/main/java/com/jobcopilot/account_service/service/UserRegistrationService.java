package com.jobcopilot.account_service.service;

import com.jobcopilot.account_service.entity.User;
import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.repository.UserRepository;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserRegistrationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Autowired
  public UserRegistrationService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void registerUser(UserRegistrationRequest userRegistrationRequest) {
    final String email = normalizeEmail(userRegistrationRequest.email());
    userRepository
        .findByEmail(email)
        .ifPresentOrElse(
            _ -> {
              throw new UserExistsException(userRegistrationRequest.email());
            },
            () -> {
              userRepository.save(buildUser(userRegistrationRequest, email));
            });
  }

  private String normalizeEmail(String email) {
    return email.toLowerCase().trim();
  }

  private User buildUser(UserRegistrationRequest userRegistrationRequest, String email) {
    final String encodedPassword = passwordEncoder.encode(userRegistrationRequest.password());

    return User.builder()
        .userId(UUID.randomUUID())
        .email(email)
        .passwordHash(encodedPassword)
        .active(true)
        .build();
  }
}
