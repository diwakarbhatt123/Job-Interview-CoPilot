package com.jobcopilot.job_analyzer_service.entity.values;

import com.jobcopilot.job_analyzer_service.enums.InputType;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record Input(
    InputType inputType,
    String url,
    String rawText,
    String fetchedText,
    String normalizedText,
    String language,
    Instant submittedAt) {}
