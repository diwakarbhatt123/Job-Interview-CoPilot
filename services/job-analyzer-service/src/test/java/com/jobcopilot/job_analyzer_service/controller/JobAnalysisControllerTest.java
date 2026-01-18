package com.jobcopilot.job_analyzer_service.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import com.jobcopilot.job_analyzer_service.enums.InputType;
import com.jobcopilot.job_analyzer_service.exception.ProfileOwnershipException;
import com.jobcopilot.job_analyzer_service.model.request.SubmitJobAnalysisRequest;
import com.jobcopilot.job_analyzer_service.model.response.JobAnalysisResponse;
import com.jobcopilot.job_analyzer_service.service.JobAnalysisService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(JobAnalysisController.class)
class JobAnalysisControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private JobAnalysisService jobAnalysisService;

  @Test
  void submitJobAnalysis_missingUserIdHeader_returns401() throws Exception {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, "text", null, null, null);

    mockMvc
        .perform(
            post("/job/analysis/submit")
                .header("X-User-Id", "  ")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.error").value("Missing or invalid X-User-Id header"));
  }

  @Test
  void submitJobAnalysis_invalidPayload_returns400() throws Exception {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, null, null, null, null);

    mockMvc
        .perform(
            post("/job/analysis/submit")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error").value("Invalid input for request type."));
  }

  @Test
  void submitJobAnalysis_profileNotOwned_returns403() throws Exception {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, "text", null, null, null);

    when(jobAnalysisService.submitJobAnalysis(request, "user-1"))
        .thenThrow(new ProfileOwnershipException("profile-1"));

    mockMvc
        .perform(
            post("/job/analysis/submit")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden())
        .andExpect(
            jsonPath("$.error")
                .value("Profile does not belong to the authenticated user: profile-1"));
  }

  @Test
  void submitJobAnalysis_success_returns202() throws Exception {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, "text", null, null, null);
    JobAnalysisResponse response =
        JobAnalysisResponse.builder()
            .jobId("job-1")
            .profileId("profile-1")
            .status(AnalysisStatus.PENDING)
            .submittedAt(java.time.Instant.now())
            .build();

    when(jobAnalysisService.submitJobAnalysis(request, "user-1")).thenReturn(response);

    mockMvc
        .perform(
            post("/job/analysis/submit")
                .header("X-User-Id", "user-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.jobId").value("job-1"))
        .andExpect(jsonPath("$.profileId").value("profile-1"))
        .andExpect(jsonPath("$.status").value("PENDING"));
  }
}
