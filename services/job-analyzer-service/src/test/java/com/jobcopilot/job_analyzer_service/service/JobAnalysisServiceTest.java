package com.jobcopilot.job_analyzer_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import com.jobcopilot.job_analyzer_service.enums.InputType;
import com.jobcopilot.job_analyzer_service.exception.ProfileOwnershipException;
import com.jobcopilot.job_analyzer_service.model.request.SubmitJobAnalysisRequest;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class JobAnalysisServiceTest {
  private JobRepository jobRepository;
  private ProfileOwnershipService profileOwnershipService;
  private JobAnalysisService jobAnalysisService;

  @BeforeEach
  void setUp() {
    jobRepository = Mockito.mock(JobRepository.class);
    profileOwnershipService = Mockito.mock(ProfileOwnershipService.class);
    jobAnalysisService = new JobAnalysisService(jobRepository, profileOwnershipService);
  }

  @Test
  void submitJobAnalysis_rejectsWhenProfileNotOwned() {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, "text", null, null, null);

    when(profileOwnershipService.isOwnedByUser("profile-1", "user-1")).thenReturn(false);

    assertThatThrownBy(() -> jobAnalysisService.submitJobAnalysis(request, "user-1"))
        .isInstanceOf(ProfileOwnershipException.class);
    verify(jobRepository, never()).save(any(Job.class));
  }

  @Test
  void submitJobAnalysis_pastedCreatesPendingJob() {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest("profile-1", InputType.PASTED, "raw text", null, null, null);

    when(profileOwnershipService.isOwnedByUser("profile-1", "user-1")).thenReturn(true);
    when(jobRepository.save(any(Job.class)))
        .thenAnswer(
            invocation -> {
              Job input = invocation.getArgument(0);
              return input.toBuilder().id("job-1").build();
            });

    var response = jobAnalysisService.submitJobAnalysis(request, "user-1");

    ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
    verify(jobRepository).save(captor.capture());
    Job saved = captor.getValue();

    assertThat(saved.getUserId()).isEqualTo("user-1");
    assertThat(saved.getProfileId()).isEqualTo("profile-1");
    assertThat(saved.getAnalysis().status()).isEqualTo(AnalysisStatus.PENDING);
    assertThat(saved.getInput().inputType()).isEqualTo(InputType.PASTED);
    assertThat(saved.getInput().rawText()).isEqualTo("raw text");
    assertThat(saved.getInput().submittedAt()).isNotNull();

    assertThat(response.jobId()).isEqualTo("job-1");
    assertThat(response.profileId()).isEqualTo("profile-1");
    assertThat(response.status()).isEqualTo(AnalysisStatus.PENDING);
    assertThat(response.submittedAt()).isNotNull();
  }

  @Test
  void submitJobAnalysis_urlCreatesPendingJob() {
    SubmitJobAnalysisRequest request =
        new SubmitJobAnalysisRequest(
            "profile-2", InputType.URL, null, "https://example.com", null, null);

    when(profileOwnershipService.isOwnedByUser("profile-2", "user-2")).thenReturn(true);
    when(jobRepository.save(any(Job.class)))
        .thenAnswer(
            invocation -> {
              Job input = invocation.getArgument(0);
              return input.toBuilder().id("job-2").build();
            });

    var response = jobAnalysisService.submitJobAnalysis(request, "user-2");

    ArgumentCaptor<Job> captor = ArgumentCaptor.forClass(Job.class);
    verify(jobRepository).save(captor.capture());
    Job saved = captor.getValue();

    assertThat(saved.getUserId()).isEqualTo("user-2");
    assertThat(saved.getProfileId()).isEqualTo("profile-2");
    assertThat(saved.getAnalysis().status()).isEqualTo(AnalysisStatus.PENDING);
    assertThat(saved.getInput().inputType()).isEqualTo(InputType.URL);
    assertThat(saved.getInput().url()).isEqualTo("https://example.com");
    assertThat(saved.getInput().submittedAt()).isNotNull();

    assertThat(response.jobId()).isEqualTo("job-2");
    assertThat(response.profileId()).isEqualTo("profile-2");
    assertThat(response.status()).isEqualTo(AnalysisStatus.PENDING);
    assertThat(response.submittedAt()).isNotNull();
  }
}
