package com.jobcopilot.job_analyzer_service.entity.values;

import java.util.List;
import org.springframework.util.CollectionUtils;

public record Raw(List<SkillMention> skillMentions) {
  public Raw {
    skillMentions = CollectionUtils.isEmpty(skillMentions) ? null : List.copyOf(skillMentions);
  }

  public record SkillMention(String name, Integer count) {}
}
