package com.jobcopilot.profile_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.exception.ProfileAlreadyExistsException;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock private ProfileRepository profileRepository;

  @InjectMocks private ProfileService profileService;

  @Test
  void createProfileSucceeds() {
    CreateProfileRequest request = new CreateProfileRequest("Primary");
    when(profileRepository.existsByUserIdAndDisplayName("user-1", "Primary")).thenReturn(false);
    when(profileRepository.save(any(Profile.class)))
        .thenAnswer(
            invocation -> {
              Profile saved = invocation.getArgument(0);
              saved.setId("profile-1");
              return saved;
            });

    ProfileStatusResponse response = profileService.createProfile(request, "user-1");

    assertThat(response.id()).isEqualTo("profile-1");
    assertThat(response.status()).isEqualTo(ProfileStatus.CREATED);
  }

  @Test
  void createProfileRejectsDuplicateDisplayName() {
    CreateProfileRequest request = new CreateProfileRequest("Primary");
    when(profileRepository.existsByUserIdAndDisplayName("user-1", "Primary")).thenReturn(true);

    assertThatThrownBy(() -> profileService.createProfile(request, "user-1"))
        .isInstanceOf(ProfileAlreadyExistsException.class);
  }
}
