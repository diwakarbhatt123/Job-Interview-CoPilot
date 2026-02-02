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
    Job job =
        Job.builder()
            .id("job-1")
            .analysis(
                com.jobcopilot.job_analyzer_service.entity.values.Analysis.builder()
                    .lockedBy("poller-1")
                    .build())
            .input(Input.builder().rawText("text").build())
            .build();
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

    verify(jobRepository)
        .markCompletedWithExtracted(
            Mockito.eq("job-1"),
            Mockito.eq("poller-1"),
            any(Instant.class),
            Mockito.eq("text"),
            any(com.jobcopilot.job_analyzer_service.entity.values.Extracted.class));
    verify(jobRepository, never())
        .markFailed(Mockito.anyString(), Mockito.anyString(), any(Instant.class), any(Error.class));
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
    Job job =
        Job.builder()
            .id("job-2")
            .analysis(
                com.jobcopilot.job_analyzer_service.entity.values.Analysis.builder()
                    .lockedBy("poller-2")
                    .build())
            .input(Input.builder().rawText("text").build())
            .build();
    Mockito.when(pipeline.execute(Mockito.any())).thenThrow(new RuntimeException("boom"));

    jobAnalysisService.analyseJob(job);

    ArgumentCaptor<Error> errorCaptor = ArgumentCaptor.forClass(Error.class);
    verify(jobRepository, never())
        .markCompletedWithExtracted(
            Mockito.eq("job-2"),
            Mockito.eq("poller-2"),
            any(Instant.class),
            any(String.class),
            any(com.jobcopilot.job_analyzer_service.entity.values.Extracted.class));
    verify(jobRepository)
        .markFailed(
            Mockito.eq("job-2"), Mockito.eq("poller-2"), any(Instant.class), errorCaptor.capture());
    Error error = errorCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(error.code()).isEqualTo(ErrorCode.PARSER_FAILED);
  }

  @Test
  void sanitizesErrorMessageBeforePersisting() throws Exception {
    JobRepository jobRepository = Mockito.mock(JobRepository.class);
    com.jobcopilot.parser.ParsingPipeline pipeline =
        Mockito.mock(com.jobcopilot.parser.ParsingPipeline.class);
    ObjectProvider<com.jobcopilot.parser.ParsingPipeline> provider =
        Mockito.mock(ObjectProvider.class);
    Mockito.when(provider.getObject()).thenReturn(pipeline);
    JobAnalysisService jobAnalysisService = new JobAnalysisService(jobRepository, provider);
    Job job =
        Job.builder()
            .id("job-3")
            .analysis(
                com.jobcopilot.job_analyzer_service.entity.values.Analysis.builder()
                    .lockedBy("poller-3")
                    .build())
            .input(Input.builder().rawText("text").build())
            .build();

    String longMessage = "bad\nmessage " + "x".repeat(600);
    Mockito.when(pipeline.execute(Mockito.any())).thenThrow(new RuntimeException(longMessage));

    jobAnalysisService.analyseJob(job);

    ArgumentCaptor<Error> errorCaptor = ArgumentCaptor.forClass(Error.class);
    verify(jobRepository)
        .markFailed(
            Mockito.eq("job-3"), Mockito.eq("poller-3"), any(Instant.class), errorCaptor.capture());
    Error error = errorCaptor.getValue();
    org.assertj.core.api.Assertions.assertThat(error.message()).doesNotContain("\n");
    org.assertj.core.api.Assertions.assertThat(error.message().length()).isLessThanOrEqualTo(500);
    org.assertj.core.api.Assertions.assertThat(error.message()).startsWith("bad message");
  }
}
