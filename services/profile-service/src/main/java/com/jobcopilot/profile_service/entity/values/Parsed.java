package com.jobcopilot.profile_service.entity.values;

import java.util.List;

public record Parsed(
    List<String> skills,
    Integer yearsOfExperience,
    List<Experience> experiences,
    List<Education> educations,
    List<Award> awards) {
  public Parsed {
    skills = List.copyOf(skills);
    experiences = List.copyOf(experiences);
    educations = List.copyOf(educations);
    awards = List.copyOf(awards);
  }
}
