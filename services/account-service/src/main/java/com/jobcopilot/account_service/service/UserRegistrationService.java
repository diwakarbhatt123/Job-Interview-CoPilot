package com.jobcopilot.account_service.service;

import com.jobcopilot.account_service.entity.User;
import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.repository.UserRepository;
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
    final String email = userRegistrationRequest.email().toLowerCase().trim();
    userRepository
        .findByEmail(email)
        .ifPresentOrElse(
            _ -> {
              throw new UserExistsException(userRegistrationRequest.email());
            },
            () -> {
              final String password = userRegistrationRequest.password();
              final String encodedPassword = passwordEncoder.encode(password);

              final User newUser =
                  User.builder().email(email).passwordHash(encodedPassword).active(true).build();

              userRepository.save(newUser);
            });
  }
}
