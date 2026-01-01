package com.jobcopilot.profile_service.parser.model.request;

import org.springframework.web.multipart.MultipartFile;

public record PDFAnalysisPipelineRequest(MultipartFile multipartFile) implements PipelineRequest {}
