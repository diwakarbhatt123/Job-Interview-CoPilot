package com.jobcopilot.job_analyzer_service.model.request;

import com.jobcopilot.job_analyzer_service.enums.InputType;
import com.jobcopilot.job_analyzer_service.validator.InputTypeValidation;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

@InputTypeValidation
public record SubmitJobAnalysisRequest(
    @NotNull String profileId,
    @NotNull InputType type,
    String text,
    @URL(protocol = "http", regexp = "^(https?://).+") String url,
    String displayName,
    String sourceLabel) {}
