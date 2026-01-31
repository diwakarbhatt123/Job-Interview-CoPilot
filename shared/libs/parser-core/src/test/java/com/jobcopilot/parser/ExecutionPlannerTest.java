package com.jobcopilot.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.jobcopilot.parser.model.StageGroup;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExecutionPlannerTest {

  @Test
  void groupsSequentialAndParallelStages() {
    List<PipelineStage> stages =
        List.of(
            new TestStage(false),
            new TestStage(false),
            new TestStage(true),
            new TestStage(true),
            new TestStage(false));

    List<StageGroup> groups = ExecutionPlanner.toExecutionPlan(stages);

    assertEquals(3, groups.size());
    assertEquals(false, groups.get(0).parallel());
    assertEquals(2, groups.get(0).stages().size());
    assertEquals(true, groups.get(1).parallel());
    assertEquals(2, groups.get(1).stages().size());
    assertEquals(false, groups.get(2).parallel());
    assertEquals(1, groups.get(2).stages().size());
  }

  private static final class TestStage implements PipelineStage {
    private final boolean parallel;

    private TestStage(boolean parallel) {
      this.parallel = parallel;
    }

    @Override
    public boolean isParallelizable() {
      return parallel;
    }

    @Override
    public com.jobcopilot.parser.model.output.StageOutput process(
        com.jobcopilot.parser.model.input.StageInput input) {
      throw new UnsupportedOperationException("Not used in test");
    }
  }
}
