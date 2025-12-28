package com.jobcopilot.profile_service.controller;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.model.response.ProfileSummaryResponse;
import com.jobcopilot.profile_service.service.ProfileService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ProfileController.class)
@EnableAutoConfiguration(
    excludeName = {
      "org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration",
      "org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration",
      "org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration"
    })
@Import(ProfileControllerTest.MongoTestConfig.class)
class ProfileControllerTest {

  @Autowired private MockMvc mockMvc;
  @MockitoBean private ProfileService profileService;

  @Test
  void createProfileReturnsCreated() throws Exception {
    when(profileService.createProfile(any(CreateProfileRequest.class), any(String.class)))
        .thenReturn(new ProfileStatusResponse("profile-1", ProfileStatus.CREATED));

    mockMvc
        .perform(
            post("/profile")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\"Primary\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value("profile-1"))
        .andExpect(jsonPath("$.status").value("CREATED"));
  }

  @Test
  void createProfileRejectsBlankDisplayName() throws Exception {
    mockMvc
        .perform(
            post("/profile")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"displayName\":\" \"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void allProfilesReturnsSummaries() throws Exception {
    ProfileSummaryResponse summary =
        ProfileSummaryResponse.builder()
            .id("profile-1")
            .displayName("Primary")
            .status(ProfileStatus.CREATED)
            .created(Instant.parse("2024-01-01T00:00:00Z"))
            .updated(Instant.parse("2024-02-01T00:00:00Z"))
            .build();

    when(profileService.getProfiles("user-1")).thenReturn(List.of(summary));

    mockMvc
        .perform(get("/profile/all").header("X-User-Id", "user-1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.totalProfiles").value(1))
        .andExpect(jsonPath("$.profiles[0].id").value("profile-1"))
        .andExpect(jsonPath("$.profiles[0].displayName").value("Primary"));
  }

  @TestConfiguration
  static class MongoTestConfig {
    @Bean
    MongoMappingContext mongoMappingContext() {
      return new MongoMappingContext();
    }

    @Bean
    MongoCustomConversions mongoCustomConversions() {
      return new MongoCustomConversions(List.of());
    }
  }
}
