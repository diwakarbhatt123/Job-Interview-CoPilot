package com.jobcopilot.profile_service.model.response;

import com.jobcopilot.profile_service.enums.ProfileStatus;

public record ProfileStatusResponse(String id, ProfileStatus status) {}
