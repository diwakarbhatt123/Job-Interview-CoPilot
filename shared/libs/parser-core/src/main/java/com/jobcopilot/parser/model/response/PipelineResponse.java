package com.jobcopilot.parser.model.response;

import com.jobcopilot.parser.dictionary.Skill;
import com.jobcopilot.parser.model.output.EducationExtractedOutput;
import com.jobcopilot.parser.model.output.ExperienceExtractedOutput;
import com.jobcopilot.parser.model.output.StageOutput;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record PipelineResponse(
    String rawText,
    String normalizedText,
    List<EducationExtractedOutput.EducationEntry> educations,
    List<ExperienceExtractedOutput.ExperienceEntry> experiences,
    List<Skill> skills,
    Integer yearsOfExperience)
    implements StageOutput {
  public PipelineResponse {
    educations = educations == null ? List.of() : List.copyOf(educations);
    experiences = experiences == null ? List.of() : List.copyOf(experiences);
    skills = skills == null ? List.of() : List.copyOf(skills);
  }
}
