package com.jobcopilot.profile_service.model.response;

import java.util.List;

public record ProfilesResponse(List<ProfileSummaryResponse> profiles, int totalProfiles) {
  public ProfilesResponse {
    profiles = List.copyOf(profiles);
  }
}
