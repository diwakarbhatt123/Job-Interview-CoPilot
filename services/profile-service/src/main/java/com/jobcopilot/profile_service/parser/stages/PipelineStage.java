package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;

public interface PipelineStage {
  StageOutput process(StageInput input);

  default boolean isParallelizable() {
    return false;
  }
}
