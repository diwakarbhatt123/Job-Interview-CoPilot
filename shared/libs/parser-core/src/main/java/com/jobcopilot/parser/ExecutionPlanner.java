package com.jobcopilot.parser;

import com.jobcopilot.parser.model.StageGroup;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExecutionPlanner {

  public List<StageGroup> toExecutionPlan(List<PipelineStage> stages) {
    List<StageGroup> groups = new ArrayList<>();
    List<PipelineStage> current = new ArrayList<>();
    Boolean currentParallel = null;

    for (PipelineStage stage : stages) {
      boolean p = stage.isParallelizable();
      if (currentParallel == null || currentParallel == p) {
        current.add(stage);
        currentParallel = p;
      } else {
        groups.add(new StageGroup(currentParallel, List.copyOf(current)));
        current.clear();
        current.add(stage);
        currentParallel = p;
      }
    }

    if (!current.isEmpty()) {
      groups.add(new StageGroup(currentParallel, List.copyOf(current)));
    }

    return groups;
  }
}
