package com.jobcopilot.profile_service.parser.model.request;

import com.jobcopilot.parser.model.request.PipelineRequest;

public record PlainTextAnalysisPipelineRequest(String plainText) implements PipelineRequest {}
