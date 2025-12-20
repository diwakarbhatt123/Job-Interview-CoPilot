package com.jobcopilot.account_service.service;

import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.repository.UserRepository;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;

  public UserLoginService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenGenerator tokenGenerator) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenGenerator = tokenGenerator;
  }

  public LoginResponse authenticateUser(UserLoginRequest userLoginRequest) {
    return userRepository
        .findByEmail(userLoginRequest.email().toLowerCase().trim())
        .map(
            user -> {
              if (passwordEncoder.matches(userLoginRequest.password(), user.getPasswordHash())) {
                String token = tokenGenerator.generateToken(user.getUserId().toString());
                return new LoginResponse(token, user.getUserId().toString());
              } else {
                throw new BadCredentialsException("Invalid email or password");
              }
            })
        .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
  }
}
