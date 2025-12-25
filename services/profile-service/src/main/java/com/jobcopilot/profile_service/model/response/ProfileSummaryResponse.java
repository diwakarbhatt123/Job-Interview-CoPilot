package com.jobcopilot.profile_service.model.response;

import java.util.UUID;

public record ProfileSummaryResponse(UUID profileId, String title, String headline) {}
