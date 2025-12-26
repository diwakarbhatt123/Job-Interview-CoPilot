package com.jobcopilot.profile_service.model.request;

import jakarta.validation.constraints.NotBlank;

public record CreateProfileRequest(@NotBlank String displayName) {}
