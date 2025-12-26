package com.jobcopilot.profile_service.service;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.exception.ProfileAlreadyExistsException;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {
  private final ProfileRepository profileRepository;

  @Autowired
  public ProfileService(ProfileRepository profileRepository) {
    this.profileRepository = profileRepository;
  }

  public ProfileStatusResponse createProfile(
      CreateProfileRequest createProfileRequest, String userId) {

    if (profileRepository.existsByUserIdAndDisplayName(
        userId, createProfileRequest.displayName())) {
      throw new ProfileAlreadyExistsException(createProfileRequest.displayName());
    }

    Profile newProfile =
        Profile.builder()
            .userId(userId)
            .displayName(createProfileRequest.displayName())
            .status(ProfileStatus.CREATED)
            .build();

    Profile savedProfile = profileRepository.save(newProfile);
    return new ProfileStatusResponse(savedProfile.getId(), savedProfile.getStatus());
  }
}
