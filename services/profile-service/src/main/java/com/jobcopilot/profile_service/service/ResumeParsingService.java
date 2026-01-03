package com.jobcopilot.profile_service.service;

import com.jobcopilot.profile_service.entity.values.*;
import com.jobcopilot.profile_service.enums.Domain;
import com.jobcopilot.profile_service.enums.ExperienceLevel;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.enums.SourceType;
import com.jobcopilot.profile_service.parser.ParsingPipeline;
import com.jobcopilot.profile_service.parser.model.request.PDFAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.model.request.PlainTextAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.repository.ProfileRepository;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.Instant;
import java.time.Year;
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
  @SuppressFBWarnings(
      value = "EI_EXPOSE_REP2",
      justification = "ParsingPipeline instances are immutable and shared safely.")
  public ResumeParsingService(
      ParsingPipeline resumeParsingPipeline,
      ParsingPipeline resumePdfParsingPipeline,
      ProfileRepository profileRepository) {
    this.resumeParsingPipeline = resumeParsingPipeline;
    this.resumePdfParsingPipeline = resumePdfParsingPipeline;
    this.profileRepository = profileRepository;
  }

  public void parseResume(String pastedResume, String profileId, Instant requestedAt) {
    log.info("Received request for parsing resume for parsingId {}", profileId);
    PlainTextAnalysisPipelineRequest pipelineRequest =
        new PlainTextAnalysisPipelineRequest(pastedResume);

    profileRepository
        .findById(profileId)
        .ifPresentOrElse(
            profile -> {
              try {
                PipelineResponse response = resumeParsingPipeline.execute(pipelineRequest);

                Resume resume = toResume(response, requestedAt, SourceType.PASTED);
                Derived derived = toDerived(response);
                profile.setDerived(derived);
                profile.setResume(resume);
                profile.setStatus(ProfileStatus.PARSING_COMPLETED);
                profileRepository.save(profile);
              } catch (Exception e) {
                log.error("Error parsing resume for profile id {}", profileId, e);
                profile.setStatus(ProfileStatus.PARSING_FAILED);
                profileRepository.save(profile);
                throw new RuntimeException(e);
              }
            },
            () -> {
              log.error("Profile {} not found.", profileId);
              throw new RuntimeException("Could not find profile by id " + profileId);
            });

    log.info("Parsing complete for profileId {}", profileId);
  }

  public void parseResumeFile(
      byte[] pdfBytes, String filename, String contentType, String profileId, Instant requestedAt) {
    log.info("Received upload parsing request for profile id {}", profileId);

    PDFAnalysisPipelineRequest pipelineRequest =
        new PDFAnalysisPipelineRequest(pdfBytes, filename, contentType);

    profileRepository
        .findById(profileId)
        .ifPresentOrElse(
            profile -> {
              try {
                PipelineResponse response = resumePdfParsingPipeline.execute(pipelineRequest);

                Resume parsedResume = toResume(response, requestedAt, SourceType.UPLOADED);
                Derived derived = toDerived(response);
                profile.setDerived(derived);
                profile.setResume(parsedResume);
                profile.setStatus(ProfileStatus.PARSING_COMPLETED);
                profileRepository.save(profile);
              } catch (Exception e) {
                log.error("Error parsing resume for profile id {}", profileId, e);
                profile.setStatus(ProfileStatus.PARSING_FAILED);
                profileRepository.save(profile);
                throw new RuntimeException(e);
              }
            },
            () -> {
              log.error("Profile {} not found.", profileId);
              throw new RuntimeException("Could not find profile by id " + profileId);
            });

    log.info("Parsing complete for profileId {}", profileId);
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
