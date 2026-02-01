package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import java.util.List;

public record ExtractedMetadataOutput(
    String rawText,
    String normalizedText,
    Seniority seniority,
    String seniorityReason,
    Domain domain,
    String domainReason,
    List<String> requiredSkills,
    List<String> preferredSkills,
    List<String> techStack)
    implements com.jobcopilot.parser.model.response.PipelineResponse {
  public ExtractedMetadataOutput {
    requiredSkills = requiredSkills == null ? List.of() : List.copyOf(requiredSkills);
    preferredSkills = preferredSkills == null ? List.of() : List.copyOf(preferredSkills);
    techStack = techStack == null ? List.of() : List.copyOf(techStack);
  }
}
