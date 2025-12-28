package com.jobcopilot.profile_service.parser.model;

import com.jobcopilot.profile_service.parser.stages.PipelineStage;
import java.util.List;
import java.util.Objects;

public record StageGroup(boolean parallel, List<PipelineStage> stages) {
  public StageGroup {
    Objects.requireNonNull(stages, "stages");
    stages = List.copyOf(stages);
  }
}
