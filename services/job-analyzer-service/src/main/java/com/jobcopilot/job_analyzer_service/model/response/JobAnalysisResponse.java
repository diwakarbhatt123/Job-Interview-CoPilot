package com.jobcopilot.job_analyzer_service.model.response;

import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record JobAnalysisResponse(
    String jobId, String profileId, AnalysisStatus status, Instant submittedAt) {}
