package com.jobcopilot.job_analyzer_service.service;

import com.jobcopilot.job_analyzer_service.exception.ProfileServiceException;
import com.jobcopilot.profile_service.client.ProfileServiceClient;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProfileOwnershipService {
  private final ProfileServiceClient profileServiceClient;

  public ProfileOwnershipService(ProfileServiceClient profileServiceClient) {
    this.profileServiceClient = profileServiceClient;
  }

  public boolean isOwnedByUser(String profileId, String userId) {
    try {
      profileServiceClient.getProfile(profileId, userId);
      return true;
    } catch (FeignException ex) {
      if (ex.status() == HttpStatus.NOT_FOUND.value()) {
        return false;
      }
      throw new ProfileServiceException("Failed to verify profile ownership", ex);
    }
  }
}
