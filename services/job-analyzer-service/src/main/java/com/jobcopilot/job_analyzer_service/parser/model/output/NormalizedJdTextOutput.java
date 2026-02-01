package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.parser.model.output.StageOutput;

public record NormalizedJdTextOutput(String rawText, String normalizedText)
    implements StageOutput {}
