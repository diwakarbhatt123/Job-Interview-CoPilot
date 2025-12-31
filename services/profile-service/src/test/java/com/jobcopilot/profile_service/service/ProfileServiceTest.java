package com.jobcopilot.profile_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.entity.values.Derived;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.exception.ProfileAlreadyExistsException;
import com.jobcopilot.profile_service.model.request.CreateProfileRequest;
import com.jobcopilot.profile_service.model.response.ProfileStatusResponse;
import com.jobcopilot.profile_service.model.response.ProfileSummaryResponse;
import com.jobcopilot.profile_service.parser.dictionary.Skill;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import com.jobcopilot.profile_service.repository.ProfileSummaryView;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

  @Mock private ProfileRepository profileRepository;
  @Mock private ExecutorService executor;

  @InjectMocks private ProfileService profileService;

  @Test
  void createProfileSucceeds() {
    CreateProfileRequest request =
        new CreateProfileRequest("Primary", "resume text", SourceType.PASTED);
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
    CreateProfileRequest request =
        new CreateProfileRequest("Primary", "resume text", SourceType.PASTED);
    when(profileRepository.existsByUserIdAndDisplayName("user-1", "Primary")).thenReturn(true);

    assertThatThrownBy(() -> profileService.createProfile(request, "user-1"))
        .isInstanceOf(ProfileAlreadyExistsException.class);
  }

  @Test
  void getProfilesMapsAndSortsByUpdatedDesc() {
    ProfileSummaryView newer =
        new TestProfileSummaryView(
            "profile-2",
            "Secondary",
            ProfileStatus.CREATED,
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-03-01T00:00:00Z"),
            Derived.builder().skillsNormalized(List.of(Skill.JAVA)).build(),
            new TestResumeView(new TestParsedView(5)));

    ProfileSummaryView older =
        new TestProfileSummaryView(
            "profile-1",
            "Primary",
            ProfileStatus.CREATED,
            Instant.parse("2024-01-01T00:00:00Z"),
            Instant.parse("2024-02-01T00:00:00Z"),
            null,
            null);

    ProfileSummaryView noUpdated =
        new TestProfileSummaryView(
            "profile-3",
            "Tertiary",
            ProfileStatus.CREATED,
            Instant.parse("2024-01-01T00:00:00Z"),
            null,
            null,
            null);

    when(profileRepository.findSummariesByUserId("user-1"))
        .thenReturn(List.of(older, noUpdated, newer));

    List<ProfileSummaryResponse> responses = profileService.getProfiles("user-1");

    assertThat(responses).hasSize(3);
    assertThat(responses.get(0).id()).isEqualTo("profile-2");
    assertThat(responses.get(1).id()).isEqualTo("profile-1");
    assertThat(responses.get(2).id()).isEqualTo("profile-3");
    assertThat(responses.get(0).summary().yearsOfExperience()).isEqualTo(5);
    assertThat(responses.get(0).summary().skills()).containsExactly(Skill.JAVA);
  }

  private record TestProfileSummaryView(
      String id,
      String displayName,
      ProfileStatus status,
      Instant createdAt,
      Instant updatedAt,
      Derived derived,
      ResumeView resume)
      implements ProfileSummaryView {
    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDisplayName() {
      return displayName;
    }

    @Override
    public ProfileStatus getStatus() {
      return status;
    }

    @Override
    public Instant getCreatedAt() {
      return createdAt;
    }

    @Override
    public Instant getUpdatedAt() {
      return updatedAt;
    }

    @Override
    public Derived getDerived() {
      return derived;
    }

    @Override
    public ResumeView getResume() {
      return resume;
    }
  }

  private record TestResumeView(ProfileSummaryView.ParsedView parsed)
      implements ProfileSummaryView.ResumeView {
    @Override
    public ProfileSummaryView.ParsedView getParsed() {
      return parsed;
    }
  }

  private record TestParsedView(Integer yearsOfExperience)
      implements ProfileSummaryView.ParsedView {
    @Override
    public Integer getYearsOfExperience() {
      return yearsOfExperience;
    }
  }
}
