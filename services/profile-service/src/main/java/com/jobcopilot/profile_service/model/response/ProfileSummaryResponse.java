package com.jobcopilot.profile_service.model.response;

import com.jobcopilot.profile_service.enums.Domain;
import com.jobcopilot.profile_service.enums.ExperienceLevel;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import com.jobcopilot.profile_service.parser.dictionary.Skill;
import java.time.Instant;
import java.util.List;
import lombok.Builder;

@Builder
public record ProfileSummaryResponse(
    String id,
    String displayName,
    Summary summary,
    ProfileStatus status,
    Instant created,
    Instant updated) {

  @Builder
  public record Summary(
      Integer yearsOfExperience,
      ExperienceLevel experienceLevel,
      List<Skill> skills,
      Domain domain) {
    public Summary {
      skills = skills == null ? List.of() : List.copyOf(skills);
    }
  }
}
