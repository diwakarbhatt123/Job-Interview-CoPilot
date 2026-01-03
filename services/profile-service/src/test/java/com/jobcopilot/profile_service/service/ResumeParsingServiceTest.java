package com.jobcopilot.profile_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.parser.ParsingPipeline;
import com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput;
import com.jobcopilot.profile_service.parser.model.output.ExperienceExtractedOutput;
import com.jobcopilot.profile_service.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResumeParsingServiceTest {

  @Mock private ParsingPipeline resumeParsingPipeline;
  @Mock private ParsingPipeline resumePdfParsingPipeline;
  @Mock private ProfileRepository profileRepository;

  private ResumeParsingService resumeParsingService;

  @org.junit.jupiter.api.BeforeEach
  void setUp() {
    resumeParsingService =
        new ResumeParsingService(
            resumeParsingPipeline, resumePdfParsingPipeline, profileRepository);
  }

  @Test
  void parseResume_setsSourceTypeAndMapsExperienceDates() throws Exception {
    Profile profile = Profile.builder().id("profile-1").status(ProfileStatus.CREATED).build();
    when(profileRepository.findById("profile-1")).thenReturn(Optional.of(profile));

    PipelineResponse response =
        PipelineResponse.builder()
            .rawText("raw")
            .normalizedText("normalized")
            .experiences(
                List.of(
                    new ExperienceExtractedOutput.ExperienceEntry(
                        "Engineer 2019 - 2021",
                        "Acme Corp",
                        "Engineer",
                        2019,
                        2021,
                        false,
                        List.of("Did work"))))
            .educations(
                List.of(
                    new EducationExtractedOutput.EducationEntry(
                        "Uni", "BSc", null, null, null, List.of())))
            .build();

    when(resumeParsingPipeline.execute(any())).thenReturn(response);
    resumeParsingService.parseResume("resume text", "profile-1", Instant.now());

    ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository, atLeastOnce()).save(captor.capture());

    Profile saved = captor.getValue();
    assertThat(saved.getResume()).isNotNull();
    assertThat(saved.getResume().source().type()).isEqualTo(SourceType.PASTED);
    assertThat(saved.getResume().parsed().experiences()).hasSize(1);
    assertThat(saved.getResume().parsed().experiences().getFirst().startAt())
        .isEqualTo(LocalDate.of(2019, 1, 1));
    assertThat(saved.getResume().parsed().experiences().getFirst().endAt())
        .isEqualTo(LocalDate.of(2021, 1, 1));
  }

  @Test
  void parseResumeFile_setsSourceTypeUploaded() throws Exception {
    Profile profile = Profile.builder().id("profile-2").status(ProfileStatus.CREATED).build();
    when(profileRepository.findById("profile-2")).thenReturn(Optional.of(profile));

    PipelineResponse response =
        PipelineResponse.builder().rawText("raw").normalizedText("normalized").build();

    when(resumePdfParsingPipeline.execute(any())).thenReturn(response);
    resumeParsingService.parseResumeFile(
        new byte[] {0x25, 0x50, 0x44, 0x46},
        "resume.pdf",
        "application/pdf",
        "profile-2",
        Instant.now());

    ArgumentCaptor<Profile> captor = ArgumentCaptor.forClass(Profile.class);
    verify(profileRepository, atLeastOnce()).save(captor.capture());

    Profile saved = captor.getValue();
    assertThat(saved.getResume()).isNotNull();
    assertThat(saved.getResume().source().type()).isEqualTo(SourceType.UPLOADED);
  }
}
