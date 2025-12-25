package com.jobcopilot.profile_service.model.response;

import java.util.List;

public record GetAllProfilesResponse(List<ProfileSummaryResponse> profiles, int totalProfiles) {}
