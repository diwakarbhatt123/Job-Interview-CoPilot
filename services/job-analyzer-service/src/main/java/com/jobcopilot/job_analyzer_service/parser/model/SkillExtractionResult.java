package com.jobcopilot.job_analyzer_service.parser.model;

import java.util.List;

public record SkillExtractionResult(
    List<String> requiredSkills, List<String> preferredSkills, List<String> techStack) {
  public SkillExtractionResult {
    requiredSkills = requiredSkills == null ? List.of() : List.copyOf(requiredSkills);
    preferredSkills = preferredSkills == null ? List.of() : List.copyOf(preferredSkills);
    techStack = techStack == null ? List.of() : List.copyOf(techStack);
  }
}
