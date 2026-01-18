package com.jobcopilot.job_analyzer_service.entity.values;

import com.jobcopilot.job_analyzer_service.enums.Domain;
import com.jobcopilot.job_analyzer_service.enums.Seniority;
import java.util.List;
import org.springframework.util.CollectionUtils;

public record Extracted(
    Seniority seniority,
    Domain domain,
    List<String> requiredSkills,
    List<String> preferredSkills,
    List<String> techStack,
    List<String> responsibilities,
    Signals signals,
    Raw raw) {
  public Extracted {
    requiredSkills = CollectionUtils.isEmpty(requiredSkills) ? null : List.copyOf(requiredSkills);
    preferredSkills =
        CollectionUtils.isEmpty(preferredSkills) ? null : List.copyOf(preferredSkills);
    techStack = CollectionUtils.isEmpty(techStack) ? null : List.copyOf(techStack);
    responsibilities =
        CollectionUtils.isEmpty(responsibilities) ? null : List.copyOf(responsibilities);
  }
}
