package com.jobcopilot.profile_service.parser.model.output;

public record NormalizedTextOutput(String normalizedText, String rawText)
    implements com.jobcopilot.parser.model.output.StageOutput {}
