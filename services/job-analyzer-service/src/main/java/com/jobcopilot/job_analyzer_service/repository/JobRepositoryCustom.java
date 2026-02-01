package com.jobcopilot.job_analyzer_service.repository;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.entity.values.Extracted;
import java.time.Instant;
import java.util.Optional;

public interface JobRepositoryCustom {
  Optional<Job> acquirePendingJob(
      String pollerId, Instant now, Instant lockExpiry, int maxAttempts);

  void markCompletedWithExtracted(
      String jobId, String lockedBy, Instant now, String normalizedText, Extracted extracted);

  void markFailed(String jobId, String lockedBy, Instant now, Error error);
}
