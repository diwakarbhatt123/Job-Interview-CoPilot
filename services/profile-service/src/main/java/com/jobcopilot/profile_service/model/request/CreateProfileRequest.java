package com.jobcopilot.profile_service.model.request;

import com.jobcopilot.profile_service.enums.SourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateProfileRequest(
    @NotBlank String displayName, @NotBlank String pastedCV, @NotNull SourceType sourceType) {}
