package com.jobcopilot.job_analyzer_service.job.service;

import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.*;
import com.jobcopilot.job_analyzer_service.entity.values.Error;
import com.jobcopilot.job_analyzer_service.enums.ErrorCode;
import com.jobcopilot.job_analyzer_service.parser.model.output.ExtractedMetadataOutput;
import com.jobcopilot.job_analyzer_service.parser.model.request.JdAnalysisPipelineRequest;
import com.jobcopilot.job_analyzer_service.repository.JobRepository;
import com.jobcopilot.parser.model.request.PipelineRequest;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JobAnalysisService {

  private final JobRepository jobRepository;
  private final ObjectProvider<com.jobcopilot.parser.ParsingPipeline> jobAnalysisPipelineProvider;

  public JobAnalysisService(
      JobRepository jobRepository,
      ObjectProvider<com.jobcopilot.parser.ParsingPipeline> jobAnalysisPipelineProvider) {
    this.jobRepository = jobRepository;
    this.jobAnalysisPipelineProvider = jobAnalysisPipelineProvider;
  }

  public void analyseJob(Job job) {
    log.info("Starting job analysis for job {}", job.getId());
    Instant now = Instant.now();
    try {

      PipelineRequest request = new JdAnalysisPipelineRequest(job.getInput().rawText());
      ExtractedMetadataOutput analysisResponse =
          (ExtractedMetadataOutput) jobAnalysisPipelineProvider.getObject().execute(request);

      jobRepository.markCompletedWithExtracted(
          job.getId(),
          job.getAnalysis().lockedBy(),
          now,
          analysisResponse.normalizedText(),
          Extracted.builder()
              .preferredSkills(analysisResponse.preferredSkills())
              .requiredSkills(analysisResponse.requiredSkills())
              .seniority(analysisResponse.seniority())
              .domain(analysisResponse.domain())
              .techStack(analysisResponse.techStack())
              .build());

      log.info("Completed job analysis for job {}", job.getId());
    } catch (Exception ex) {
      String sanitizedMessage = sanitizeErrorMessage(ex);
      Error error =
          new Error(
              ErrorCode.PARSER_FAILED, sanitizedMessage, "Job analysis processing failed", false);
      jobRepository.markFailed(job.getId(), job.getAnalysis().lockedBy(), now, error);
      log.error("Failed job analysis for job {}", job.getId(), ex);
    }
  }

  private String sanitizeErrorMessage(Exception ex) {
    String message = ex.getMessage();
    if (message == null || message.isBlank()) {
      return ex.getClass().getSimpleName();
    }
    String cleaned = message.replaceAll("\\s+", " ").trim();
    int maxLength = 500;
    if (cleaned.length() <= maxLength) {
      return cleaned;
    }
    return cleaned.substring(0, maxLength);
  }
}
