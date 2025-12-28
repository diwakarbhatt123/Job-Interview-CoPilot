package com.jobcopilot.profile_service.service;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.entity.values.Derived;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.exception.ProfileAlreadyExistsException;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.model.response.ProfileSummaryResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import com.jobcopilot.profile_service.repository.ProfileSummaryView;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public List<ProfileSummaryResponse> getProfiles(String userId) {
    return profileRepository.findSummariesByUserId(userId).stream()
        .map(this::toProfileSummary)
        .sorted(
            Comparator.comparing(
                ProfileSummaryResponse::updated, Comparator.nullsLast(Comparator.reverseOrder())))
        .collect(Collectors.toList());
  }

  private ProfileSummaryResponse toProfileSummary(ProfileSummaryView profile) {
    Optional<Derived> derived = Optional.ofNullable(profile.getDerived());
    return ProfileSummaryResponse.builder()
        .id(profile.getId())
        .status(profile.getStatus())
        .created(profile.getCreatedAt())
        .updated(profile.getUpdatedAt())
        .displayName(profile.getDisplayName())
        .summary(
            ProfileSummaryResponse.Summary.builder()
                .domain(derived.map(Derived::domain).orElse(null))
                .skills(derived.map(Derived::skillsNormalized).orElse(List.of()))
                .experienceLevel(derived.map(Derived::experienceLevel).orElse(null))
                .yearsOfExperience(
                    Optional.ofNullable(profile.getResume())
                        .map(ProfileSummaryView.ResumeView::getParsed)
                        .map(ProfileSummaryView.ParsedView::getYearsOfExperience)
                        .orElse(null))
                .build())
        .build();
  }
}
