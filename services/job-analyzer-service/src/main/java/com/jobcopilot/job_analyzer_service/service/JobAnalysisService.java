package com.jobcopilot.job_analyzer_service.service;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Analysis;
import com.jobcopilot.job_analyzer_service.entity.values.Input;
import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import com.jobcopilot.job_analyzer_service.exception.ProfileOwnershipException;
import com.jobcopilot.job_analyzer_service.model.request.SubmitJobAnalysisRequest;
import com.jobcopilot.job_analyzer_service.model.response.JobAnalysisResponse;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class JobAnalysisService {
  private final JobRepository jobRepository;
  private final ProfileOwnershipService profileOwnershipService;

  public JobAnalysisService(
      JobRepository jobRepository, ProfileOwnershipService profileOwnershipService) {
    this.jobRepository = jobRepository;
    this.profileOwnershipService = profileOwnershipService;
  }

  public JobAnalysisResponse submitJobAnalysis(SubmitJobAnalysisRequest request, String userId) {
    validateOwnership(request.profileId(), userId);
    Job job = jobRepository.save(toJobEntity(request, userId));

    return toResponse(job, request.profileId());
  }

  private Job toJobEntity(SubmitJobAnalysisRequest request, String userId) {
    return Job.builder()
        .profileId(request.profileId())
        .userId(userId)
        .input(
            Input.builder()
                .inputType(request.type())
                .url(request.url())
                .rawText(request.text())
                .submittedAt(Instant.now())
                .build())
        .analysis(Analysis.builder().status(AnalysisStatus.PENDING).build())
        .build();
  }

  private void validateOwnership(String profileId, String userId) {
    if (!profileOwnershipService.isOwnedByUser(profileId, userId)) {
      throw new ProfileOwnershipException(profileId);
    }
  }

  private JobAnalysisResponse toResponse(Job job, String profileId) {
    return JobAnalysisResponse.builder()
        .jobId(job.getId())
        .status(job.getAnalysis().status())
        .submittedAt(job.getInput().submittedAt())
        .profileId(profileId)
        .build();
  }
}
