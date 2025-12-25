package com.jobcopilot.account_service.service;

import com.jobcopilot.account_service.dto.AuthenticationResult;
import com.jobcopilot.account_service.exception.BadCredentialsException;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.response.LoginResponse;
import com.jobcopilot.account_service.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.jobcopilot.auth.generator.TokenGenerator;
import org.jobcopilot.auth.model.ValidatedToken;
import org.jobcopilot.auth.validator.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

  private final String issuer;

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenGenerator tokenGenerator;
  private final TokenValidator tokenValidator;

  @Autowired
  public UserLoginService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      TokenGenerator tokenGenerator,
      TokenValidator tokenValidator,
      @Value("${auth.jwt.issuer}") String issuer) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.tokenGenerator = tokenGenerator;
    this.tokenValidator = tokenValidator;
    this.issuer = issuer;
  }

  public AuthenticationResult authenticateUser(UserLoginRequest userLoginRequest) {
    return userRepository
        .findByEmail(userLoginRequest.email().toLowerCase().trim())
        .map(
            user -> {
              if (passwordEncoder.matches(userLoginRequest.password(), user.getPasswordHash())) {
                String token = tokenGenerator.generateToken(user.getUserId().toString());
                return new AuthenticationResult(token, user.getUserId());
              } else {
                throw new BadCredentialsException("Invalid email or password");
              }
            })
        .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
  }

  public String authenticateUserToken(String authenticationToken) {
    ValidatedToken validatedToken = tokenValidator.validateAndDecodeToken(authenticationToken);

    if (!issuer.equals(validatedToken.issuer())) {
      throw new BadCredentialsException("Invalid token issuer");
    }

    if (validatedToken.expiresAt().isBefore(Instant.now())) {
      throw new BadCredentialsException("Token has expired");
    }

    if (!userRepository.existsByUserId(UUID.fromString(validatedToken.subject()))) {
      throw new BadCredentialsException("User does not exist");
    }

    return validatedToken.subject();
  }
}
