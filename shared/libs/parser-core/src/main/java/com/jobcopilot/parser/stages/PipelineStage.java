package com.jobcopilot.parser.stages;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;

public interface PipelineStage {
  StageOutput process(StageInput input);

  default boolean isParallelizable() {
    return false;
  }
}
