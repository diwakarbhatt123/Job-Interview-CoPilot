package com.jobcopilot.account_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobcopilot.account_service.exception.UserExistsException;
import com.jobcopilot.account_service.model.request.UserRegistrationRequest;
import com.jobcopilot.account_service.service.UserRegistrationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private UserRegistrationService userRegistrationService;

  @Test
  void registerUser_returnsCreated_onHappyPath() throws Exception {
    doNothing().when(userRegistrationService).registerUser(any(UserRegistrationRequest.class));

    var request = new UserRegistrationRequest("user@example.com", "password123");

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

    var request = new UserRegistrationRequest("user@example.com", "password123");

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
}
