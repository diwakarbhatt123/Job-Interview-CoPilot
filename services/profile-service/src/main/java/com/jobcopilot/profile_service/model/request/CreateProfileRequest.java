package com.jobcopilot.profile_service.model.request;

import com.jobcopilot.profile_service.enums.SourceType;
import jakarta.validation.constraints.NotBlank;

public record CreateProfileRequest(
    @NotBlank String displayName, String pastedCV, String fileId, SourceType sourceType) {}
