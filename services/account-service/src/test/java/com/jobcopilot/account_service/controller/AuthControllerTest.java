package com.jobcopilot.account_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.jobcopilot.account_service.config.TokenConfig;
import com.jobcopilot.account_service.dto.AuthenticationResult;
import com.jobcopilot.account_service.exception.BadCredentialsException;
import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserLoginRequest;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.service.UserLoginService;
import com.jobcopilot.account_service.service.UserRegistrationService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ActiveProfiles("test")
@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserRegistrationService userRegistrationService;
  @MockitoBean private UserLoginService userLoginService;
  @MockitoBean private TokenConfig tokenConfig;

  @Test
  void registerUser_returnsCreated_onHappyPath() throws Exception {
    doNothing().when(userRegistrationService).registerUser(any(UserRegistrationRequest.class));

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"user@example.com","password":"password123"}
                    """))
        .andExpect(status().isCreated());
  }

  @Test
  void registerUser_returnsConflict_onDuplicateEmail() throws Exception {
    doThrow(new UserExistsException("user@example.com"))
        .when(userRegistrationService)
        .registerUser(any(UserRegistrationRequest.class));

    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"user@example.com","password":"password123"}
                    """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error").value("User already exists"));
  }

  @Test
  void registerUser_returnsBadRequest_onInvalidInput() throws Exception {
    mockMvc
        .perform(
            post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"not-an-email","password":"short"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());
  }

  @Test
  void loginUser_returnsOk_withToken_onValidCredentials() throws Exception {
    UUID userId = UUID.randomUUID();
    Mockito.doReturn(new AuthenticationResult("jwt-token", userId))
        .when(userLoginService)
        .authenticateUser(any(UserLoginRequest.class));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"user@example.com","password":"password123"}
                    """))
        .andExpect(status().isOk())
        .andExpect(cookie().exists("AuthToken"))
        .andExpect(cookie().value("AuthToken", "jwt-token"))
        .andExpect(jsonPath("$.userId").value(userId.toString()));
  }

  @Test
  void loginUser_returnsUnauthorized_onBadCredentials() throws Exception {
    doThrow(new BadCredentialsException("bad"))
        .when(userLoginService)
        .authenticateUser(any(UserLoginRequest.class));

    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"user@example.com","password":"password123"}
                    """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("Invalid email or password"));
  }

  @Test
  void loginUser_returnsBadRequest_onInvalidInput() throws Exception {
    mockMvc
        .perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"email":"not-an-email","password":"short"}
                    """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").exists());
  }
}
