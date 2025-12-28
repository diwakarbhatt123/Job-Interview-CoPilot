package com.jobcopilot.profile_service.parser;

import com.jobcopilot.profile_service.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PipelineBuilder {

  private final List<PipelineStage> stages = new ArrayList<>();
  private ExecutorService executor;

  public PipelineBuilder init() {
    this.executor = null;
    this.stages.clear();
    return this;
  }

  public PipelineBuilder init(ExecutorService executor) {
    this.executor = executor;
    this.stages.clear();
    return this;
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
