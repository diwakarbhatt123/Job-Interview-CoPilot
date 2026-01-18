package com.jobcopilot.job_analyzer_service.entity.values;

import com.jobcopilot.job_analyzer_service.enums.InputType;
import java.time.Instant;

public record Input(
    InputType inputType,
    String url,
    String rawText,
    String fetchedText,
    String normalizedText,
    String language,
    Instant submittedAt) {}
