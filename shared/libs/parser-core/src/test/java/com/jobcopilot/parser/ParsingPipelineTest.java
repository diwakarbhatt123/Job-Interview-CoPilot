package com.jobcopilot.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.ParallelOutputs;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.model.request.PipelineRequest;
import com.jobcopilot.parser.model.response.PipelineResponse;
import com.jobcopilot.parser.stages.PipelineStage;
import org.junit.jupiter.api.Test;

class ParsingPipelineTest {

  @Test
  void runsSequentialStagesAndReturnsPipelineResponse() throws Exception {
    ParsingPipeline pipeline =
        PipelineBuilder.init().addStage(new AppendStage("A")).addStage(new FinalStage()).build();

    PipelineResponse response = pipeline.execute(new TestRequest("x"));

    assertEquals("xA", response.rawText());
  }

  @Test
  void runsParallelStagesAndMergesOutputs() throws Exception {
    ParsingPipeline pipeline =
        PipelineBuilder.init()
            .addStage(new ParallelStage("L"))
            .addStage(new ParallelStage("R"))
            .addStage(new ParallelMergeStage())
            .build();

    PipelineResponse response = pipeline.execute(new TestRequest("x"));

    assertNotNull(response.normalizedText());
    assertEquals("xLR", response.normalizedText());
  }

  private record TestRequest(String value) implements PipelineRequest {}

  private record TestOutput(String value) implements StageOutput {}

  private static final class AppendStage implements PipelineStage {
    private final String suffix;

    private AppendStage(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public StageOutput process(StageInput input) {
      if (!(input instanceof TestRequest request)) {
        throw new IllegalArgumentException("Expected TestRequest");
      }
      return new TestOutput(request.value() + suffix);
    }
  }

  private static final class FinalStage implements PipelineStage {
    @Override
    public StageOutput process(StageInput input) {
      if (!(input instanceof TestOutput output)) {
        throw new IllegalArgumentException("Expected TestOutput");
      }
      return PipelineResponse.builder()
          .rawText(output.value())
          .normalizedText(output.value())
          .build();
    }
  }

  private static final class ParallelStage implements PipelineStage {
    private final String suffix;

    private ParallelStage(String suffix) {
      this.suffix = suffix;
    }

    @Override
    public boolean isParallelizable() {
      return true;
    }

    @Override
    public StageOutput process(StageInput input) {
      if (!(input instanceof TestRequest request)) {
        throw new IllegalArgumentException("Expected TestRequest");
      }
      return new TestOutput(request.value() + suffix);
    }
  }

  private static final class ParallelMergeStage implements PipelineStage {
    @Override
    public StageOutput process(StageInput input) {
      if (!(input instanceof ParallelOutputs outputs)) {
        throw new IllegalArgumentException("Expected ParallelOutputs");
      }
      String merged =
          outputs.outputs().stream()
              .filter(TestOutput.class::isInstance)
              .map(TestOutput.class::cast)
              .map(TestOutput::value)
              .reduce("", (a, b) -> a + b);

      return PipelineResponse.builder().normalizedText(merged).build();
    }
  }
}
