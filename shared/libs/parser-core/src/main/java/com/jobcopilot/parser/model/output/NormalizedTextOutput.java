package com.jobcopilot.parser.model.output;

public record NormalizedTextOutput(String normalizedText, String rawText) implements StageOutput {}
