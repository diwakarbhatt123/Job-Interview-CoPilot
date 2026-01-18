package com.jobcopilot.job_analyzer_service.entity.values;

import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record Analysis(
    AnalysisStatus status,
    int attempt,
    String lockedBy,
    Instant lockedAt,
    Instant startedAt,
    Instant completedAt,
    Instant failedAt,
    Error error) {}
