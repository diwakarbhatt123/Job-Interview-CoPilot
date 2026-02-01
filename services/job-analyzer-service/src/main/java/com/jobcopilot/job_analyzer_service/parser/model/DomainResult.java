package com.jobcopilot.job_analyzer_service.parser.model;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;

public record DomainResult(Domain domain, String reason) {}
