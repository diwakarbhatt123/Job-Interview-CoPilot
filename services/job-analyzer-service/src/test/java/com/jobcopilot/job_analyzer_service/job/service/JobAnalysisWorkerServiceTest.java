package com.jobcopilot.job_analyzer_service.job.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.entity.values.Input;
import com.jobcopilot.job_analyzer_service.enums.ErrorCode;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.output.ExtractedMetadataOutput;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;

class JobAnalysisWorkerServiceTest {
  @Test
  void marksCompletedOnSuccess() throws Exception {
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    com.jobcopilot.parser.ParsingPipeline pipeline =
        Mockito.mock(com.jobcopilot.parser.ParsingPipeline.class);
    ObjectProvider<com.jobcopilot.parser.ParsingPipeline> provider =
        Mockito.mock(ObjectProvider.class);
    Mockito.when(provider.getObject()).thenReturn(pipeline);
    JobAnalysisService jobAnalysisService = new JobAnalysisService(jobRepository, provider);
    Job job = Job.builder().id("job-1").input(Input.builder().rawText("text").build()).build();
    Job hydratedJob = job.toBuilder().input(Input.builder().rawText("text").build()).build();

    Mockito.when(jobRepository.findById("job-1")).thenReturn(Optional.of(hydratedJob));
    Mockito.when(pipeline.execute(Mockito.any()))
        .thenReturn(
            new ExtractedMetadataOutput(
                "text",
                "text",
                Seniority.UNKNOWN,
                "",
                Domain.UNKNOWN,
                "",
                java.util.List.of(),
                java.util.List.of(),
                java.util.List.of()));

    jobAnalysisService.analyseJob(job);

    verify(jobRepository).markCompleted(Mockito.eq("job-1"), any(Instant.class));
    verify(jobRepository, never())
        .markFailed(Mockito.anyString(), any(Instant.class), any(Error.class));
  }

  @Test
  void marksFailedOnException() throws Exception {
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    com.jobcopilot.parser.ParsingPipeline pipeline =
        Mockito.mock(com.jobcopilot.parser.ParsingPipeline.class);
    ObjectProvider<com.jobcopilot.parser.ParsingPipeline> provider =
        Mockito.mock(ObjectProvider.class);
    Mockito.when(provider.getObject()).thenReturn(pipeline);
    JobAnalysisService jobAnalysisService = new JobAnalysisService(jobRepository, provider);
    Job job = Job.builder().id("job-2").input(Input.builder().rawText("text").build()).build();
    Job hydratedJob = job.toBuilder().input(Input.builder().rawText("text").build()).build();

    Mockito.when(jobRepository.findById("job-2")).thenReturn(Optional.of(hydratedJob));
    Mockito.when(pipeline.execute(Mockito.any())).thenThrow(new RuntimeException("boom"));

    jobAnalysisService.analyseJob(job);

    ArgumentCaptor<Error> errorCaptor = ArgumentCaptor.forClass(Error.class);
    verify(jobRepository, never()).markCompleted(Mockito.eq("job-2"), any(Instant.class));
    verify(jobRepository)
        .markFailed(Mockito.eq("job-2"), any(Instant.class), errorCaptor.capture());
    Error error = errorCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(error.code()).isEqualTo(ErrorCode.PARSER_FAILED);
  }
}
