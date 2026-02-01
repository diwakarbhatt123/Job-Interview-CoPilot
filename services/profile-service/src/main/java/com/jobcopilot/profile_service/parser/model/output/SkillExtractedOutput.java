package com.jobcopilot.profile_service.parser.model.output;

import com.jobcopilot.profile_service.parser.model.dictionary.Skill;
import java.util.List;

public record SkillExtractedOutput(List<Skill> skills)
    implements com.jobcopilot.parser.model.output.StageOutput {
  public SkillExtractedOutput {
    skills = skills == null ? List.of() : List.copyOf(skills);
  }
}
