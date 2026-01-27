package com.jobcopilot.profile_service.service;

import com.jobcopilot.profile_service.entity.values.*;
import com.jobcopilot.profile_service.entity.Profile;
import com.jobcopilot.profile_service.enums.Domain;
import com.jobcopilot.profile_service.enums.ExperienceLevel;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.parser.ParsingPipeline;
import com.jobcopilot.profile_service.parser.model.request.PDFAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.model.request.PlainTextAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import java.time.Instant;
import java.time.Year;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ResumeParsingService {

  private final ParsingPipeline resumeParsingPipeline;
  private final ParsingPipeline resumePdfParsingPipeline;
  private final ProfileRepository profileRepository;

  @Autowired
  public ResumeParsingService(
      ParsingPipeline resumeParsingPipeline,
      ParsingPipeline resumePdfParsingPipeline,
      ProfileRepository profileRepository) {
    this.resumeParsingPipeline = new ParsingPipeline(resumeParsingPipeline);
    this.resumePdfParsingPipeline = new ParsingPipeline(resumePdfParsingPipeline);
    this.profileRepository = profileRepository;
  }

  public void parseResume(String pastedResume, String profileId, Instant requestedAt) {
    log.info("Received request for parsing resume for parsingId {}", profileId);
    PlainTextAnalysisPipelineRequest pipelineRequest =
        new PlainTextAnalysisPipelineRequest(pastedResume);
    parseAndPersist(
        profileId,
        requestedAt,
        SourceType.PASTED,
        () -> resumeParsingPipeline.execute(pipelineRequest));
    log.info("Parsing complete for profileId {}", profileId);
  }

  public void parseResumeFile(
      byte[] pdfBytes, String filename, String contentType, String profileId, Instant requestedAt) {
    log.info("Received upload parsing request for profile id {}", profileId);

    PDFAnalysisPipelineRequest pipelineRequest =
        new PDFAnalysisPipelineRequest(pdfBytes, filename, contentType);

    parseAndPersist(
        profileId,
        requestedAt,
        SourceType.UPLOADED,
        () -> resumePdfParsingPipeline.execute(pipelineRequest));
    log.info("Parsing complete for profileId {}", profileId);
  }

  private void parseAndPersist(
      String profileId,
      Instant requestedAt,
      SourceType sourceType,
      Supplier<PipelineResponse> pipelineSupplier) {
    Profile profile = fetchProfile(profileId);
    try {
      PipelineResponse response = pipelineSupplier.get();
      applyPipelineResult(profile, response, requestedAt, sourceType);
      profile.setStatus(ProfileStatus.PARSING_COMPLETED);
    } catch (Exception e) {
      log.error("Error parsing resume for profile id {}", profileId, e);
      profile.setStatus(ProfileStatus.PARSING_FAILED);
      throw new RuntimeException(e);
    } finally {
      profileRepository.save(profile);
    }
  }

  private Profile fetchProfile(String profileId) {
    return profileRepository
        .findById(profileId)
        .orElseThrow(
            () -> {
              log.error("Profile {} not found.", profileId);
              return new RuntimeException("Could not find profile by id " + profileId);
            });
  }

  private void applyPipelineResult(
      Profile profile, PipelineResponse response, Instant requestedAt, SourceType sourceType) {
    Resume resume = toResume(response, requestedAt, sourceType);
    Derived derived = toDerived(response);
    profile.setDerived(derived);
    profile.setResume(resume);
  }

  private Resume toResume(PipelineResponse response, Instant requestedAt, SourceType sourceType) {
    return Resume.builder()
        .rawText(response.rawText())
        .source(Source.builder().type(sourceType).uploadedAt(requestedAt).build())
        .parsed(
            Parsed.builder()
                .yearsOfExperience(response.yearsOfExperience())
                .skills(response.skills())
                .educations(
                    response.educations().stream()
                        .map(
                            educationEntry ->
                                Education.builder()
                                    .field(educationEntry.field())
                                    .degree(educationEntry.degree())
                                    .institution(educationEntry.institution())
                                    .build())
                        .collect(Collectors.toList()))
                .experiences(
                    response.experiences().stream()
                        .map(
                            experienceEntry ->
                                Experience.builder()
                                    .role(experienceEntry.role())
                                    .company(experienceEntry.company())
                                    .startAt(
                                        experienceEntry.startYear() == null
                                            ? null
                                            : Year.parse(
                                                    String.valueOf(experienceEntry.startYear()))
                                                .atDay(1))
                                    .endAt(
                                        experienceEntry.endYear() == null
                                            ? null
                                            : Year.parse(String.valueOf(experienceEntry.endYear()))
                                                .atDay(1))
                                    .isCurrent(experienceEntry.isCurrent())
                                    .details(experienceEntry.details())
                                    .build())
                        .collect(Collectors.toList()))
                .build())
        .build();
  }

  private Derived toDerived(PipelineResponse response) {
    return Derived.builder()
        .domain(Domain.TECHNOLOGY)
        .experienceLevel(ExperienceLevel.SENIOR_LEVEL)
        .skillsNormalized(response.skills())
        .build();
  }
}
