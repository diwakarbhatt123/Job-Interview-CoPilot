package com.jobcopilot.job_analyzer_service.entity.values;

import com.jobcopilot.job_analyzer_service.enums.ErrorCode;

public record Error(ErrorCode code, String message, String detail, Boolean retryable) {}
