package com.jobcopilot.profile_service.parser.model.response;

import com.jobcopilot.profile_service.parser.model.dictionary.Skill;
import com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput;
import com.jobcopilot.profile_service.parser.model.output.ExperienceExtractedOutput;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record AnalysisPipelineResponse(
    String rawText,
    String normalizedText,
    List<EducationExtractedOutput.EducationEntry> educations,
    List<ExperienceExtractedOutput.ExperienceEntry> experiences,
    List<Skill> skills,
    Integer yearsOfExperience)
    implements com.jobcopilot.parser.model.response.PipelineResponse {
  public AnalysisPipelineResponse {
    educations = educations == null ? List.of() : List.copyOf(educations);
    experiences = experiences == null ? List.of() : List.copyOf(experiences);
    skills = skills == null ? List.of() : List.copyOf(skills);
  }
}
