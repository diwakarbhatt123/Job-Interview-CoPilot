package com.jobcopilot.job_analyzer_service.job;

import com.jobcopilot.job_analyzer_service.job.service.JobAnalysisService;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PendingJobAnalysisPoller {
  private final String pollerId;
  private final long lockTtlMs;
  private final int maxAttempts;
  private final ExecutorService executor;
  private final JobRepository jobRepository;
  private final JobAnalysisService jobAnalysisService;

  public PendingJobAnalysisPoller(
      @Value("${poller.id}") String pollerId,
      @Value("${poller.lockTtlMs:300000}") long lockTtlMs,
      @Value("${poller.maxAttempts:3}") int maxAttempts,
      ExecutorService executor,
      JobRepository jobRepository,
      JobAnalysisService jobAnalysisService) {
    this.pollerId = pollerId;
    this.lockTtlMs = lockTtlMs;
    this.maxAttempts = maxAttempts;
    this.executor = executor;
    this.jobRepository = jobRepository;
    this.jobAnalysisService = jobAnalysisService;
  }

  @Scheduled(fixedRateString = "${poller.intervalMs:30000}")
  private void getPendingJob() {
    if (executor instanceof ThreadPoolExecutor threadPool) {
      if (threadPool.getQueue().remainingCapacity() == 0) {
        log.warn("Job analysis queue is full. Skipping poll cycle.");
        return;
      }
    }
    Instant now = Instant.now();
    Instant lockExpiry = now.minusMillis(lockTtlMs);
    jobRepository
        .acquirePendingJob(pollerId, now, lockExpiry, maxAttempts)
        .ifPresentOrElse(
            job -> executor.submit(() -> jobAnalysisService.analyseJob(job)),
            () -> log.debug("No pending job found"));
  }
}
