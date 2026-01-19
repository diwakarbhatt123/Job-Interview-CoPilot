package com.jobcopilot.job_analyzer_service.job.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.enums.ErrorCode;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class JobAnalysisWorkerServiceTest {
  @Test
  void marksCompletedOnSuccess() {
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    JobAnalysisService jobAnalysisService = new JobAnalysisService(jobRepository);
    Job job = Job.builder().id("job-1").build();

    jobAnalysisService.analyseJob(job);

    verify(jobRepository).markCompleted(Mockito.eq("job-1"), any(Instant.class));
    verify(jobRepository, never())
        .markFailed(Mockito.anyString(), any(Instant.class), any(Error.class));
  }

  @Test
  void marksFailedOnException() {
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    JobAnalysisService jobAnalysisService = new JobAnalysisService(jobRepository);
    Job job = Job.builder().id("job-2").build();

    doThrow(new RuntimeException("boom"))
        .when(jobRepository)
        .markCompleted(Mockito.eq("job-2"), any(Instant.class));

    jobAnalysisService.analyseJob(job);

    ArgumentCaptor<Error> errorCaptor = ArgumentCaptor.forClass(Error.class);
    verify(jobRepository).markCompleted(Mockito.eq("job-2"), any(Instant.class));
    verify(jobRepository)
        .markFailed(Mockito.eq("job-2"), any(Instant.class), errorCaptor.capture());
    Error error = errorCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(error.code()).isEqualTo(ErrorCode.PARSER_FAILED);
  }
}
