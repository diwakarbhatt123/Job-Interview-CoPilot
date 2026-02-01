package com.jobcopilot.job_analyzer_service.parser.model.request;

import com.jobcopilot.parser.model.request.PipelineRequest;

public record JdAnalysisPipelineRequest(String rawText) implements PipelineRequest {}
