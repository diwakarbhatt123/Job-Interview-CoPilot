package com.jobcopilot.profile_service.entity.values;

import com.jobcopilot.profile_service.parser.dictionary.Skill;
import java.util.List;
import lombok.Builder;
import org.springframework.util.CollectionUtils;

@Builder(toBuilder = true)
public record Parsed(
    List<Skill> skills,
    Integer yearsOfExperience,
    List<Experience> experiences,
    List<Education> educations,
    List<Award> awards) {
  public Parsed {
    skills = CollectionUtils.isEmpty(skills) ? null : List.copyOf(skills);
    experiences = CollectionUtils.isEmpty(experiences) ? null : List.copyOf(experiences);
    educations = CollectionUtils.isEmpty(educations) ? null : List.copyOf(educations);
    awards = CollectionUtils.isEmpty(awards) ? null : List.copyOf(awards);
  }
}
