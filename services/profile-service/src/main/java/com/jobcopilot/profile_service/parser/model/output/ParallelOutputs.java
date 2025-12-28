package com.jobcopilot.profile_service.parser.model.output;

import com.jobcopilot.profile_service.parser.model.input.StageInput;
import java.util.List;

public record ParallelOutputs(StageInput originalInput, List<StageOutput> outputs)
    implements StageOutput {
  public ParallelOutputs {
    outputs = outputs == null ? List.of() : List.copyOf(outputs);
  }
}
