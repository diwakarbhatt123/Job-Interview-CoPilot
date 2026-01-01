package com.jobcopilot.profile_service.parser;

import com.jobcopilot.profile_service.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PipelineBuilder {

  private final List<PipelineStage> stages = new ArrayList<>();
  private final ExecutorService executor;

  private PipelineBuilder(ExecutorService executorService) {
    this.executor = executorService;
  }

  public static PipelineBuilder init() {
    return new PipelineBuilder(null);
  }

  public static PipelineBuilder init(ExecutorService executor) {
    return new PipelineBuilder(executor);
  }

  public PipelineBuilder addStage(PipelineStage stage) {
    this.stages.add(stage);
    return this;
  }

  public ParsingPipeline build() {
    ParsingPipeline pipeline =
        (executor == null) ? new ParsingPipeline() : new ParsingPipeline(executor);
    for (PipelineStage stage : stages) {
      pipeline.addStage(stage);
    }
    return pipeline;
  }
}
