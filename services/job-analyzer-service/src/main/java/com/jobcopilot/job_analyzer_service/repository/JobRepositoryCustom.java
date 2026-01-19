package com.jobcopilot.job_analyzer_service.repository;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import java.time.Instant;
import java.util.Optional;

public interface JobRepositoryCustom {
  Optional<Job> acquirePendingJob(
      String pollerId, Instant now, Instant lockExpiry, int maxAttempts);

  void markCompleted(String jobId, Instant now);

  void markFailed(String jobId, Instant now, Error error);
}
