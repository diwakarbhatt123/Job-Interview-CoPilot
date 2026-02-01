package com.jobcopilot.profile_service.entity.values;

import com.jobcopilot.profile_service.enums.Domain;
import com.jobcopilot.profile_service.enums.ExperienceLevel;
import com.jobcopilot.profile_service.parser.model.dictionary.Skill;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record Derived(
    ExperienceLevel experienceLevel, Domain domain, List<Skill> skillsNormalized) {
  public Derived {
    skillsNormalized = List.copyOf(skillsNormalized);
  }
}
