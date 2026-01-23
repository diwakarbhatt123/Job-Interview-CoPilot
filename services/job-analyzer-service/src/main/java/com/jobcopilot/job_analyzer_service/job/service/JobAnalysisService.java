package com.jobcopilot.job_analyzer_service.job.service;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.enums.ErrorCode;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobAnalysisService {

  private final JobRepository jobRepository;

  public JobAnalysisService(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  public void analyseJob(Job job) {
    log.info("Starting job analysis for job {}", job.getId());
    Instant now = Instant.now();
    try {
      completeJob(job, now);
    } catch (RuntimeException ex) {
      handleFailure(job, now, ex);
    }
  }

  private void completeJob(Job job, Instant completedAt) {
    // Stubbed analysis for M3.3.
    jobRepository.markCompleted(job.getId(), completedAt);
    log.info("Completed job analysis for job {}", job.getId());
  }

  private void handleFailure(Job job, Instant failedAt, RuntimeException ex) {
    Error error = buildError(ex);
    jobRepository.markFailed(job.getId(), failedAt, error);
    log.error("Failed job analysis for job {}", job.getId(), ex);
  }

  private Error buildError(RuntimeException ex) {
    return new Error(
        ErrorCode.PARSER_FAILED, ex.getMessage(), "Job analysis processing failed", false);
  }
}
