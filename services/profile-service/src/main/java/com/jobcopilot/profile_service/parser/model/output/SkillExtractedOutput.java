package com.jobcopilot.profile_service.parser.model.output;

import com.jobcopilot.profile_service.parser.dictionary.Skill;
import java.util.List;

public record SkillExtractedOutput(List<Skill> skills) implements StageOutput {
  public SkillExtractedOutput {
    skills = skills == null ? List.of() : List.copyOf(skills);
  }
}
