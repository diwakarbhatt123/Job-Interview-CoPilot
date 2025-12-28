package com.jobcopilot.profile_service.parser.model.request;

public record PDFAnalysisPipelineRequest(String uploadedFileId) implements PipelineRequest {}
