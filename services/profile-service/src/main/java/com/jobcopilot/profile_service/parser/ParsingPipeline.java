package com.jobcopilot.profile_service.parser;

import com.jobcopilot.profile_service.parser.model.StageGroup;
import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.ParallelOutputs;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;
import com.jobcopilot.profile_service.parser.model.request.PipelineRequest;
import com.jobcopilot.profile_service.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ParsingPipeline implements AutoCloseable {

  private final List<PipelineStage> stages = new ArrayList<>();
  private final ExecutorService executor;

  public ParsingPipeline() {
    this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
  }

  public ParsingPipeline(ExecutorService executor) {
    this.executor = executor;
  }

  public void addStage(PipelineStage stage) {
    this.stages.add(stage);
  }

  public PipelineResponse execute(PipelineRequest pipelineRequest) throws Exception {
    StageInput currentInput = pipelineRequest;

    List<StageGroup> plan = ExecutionPlanner.toExecutionPlan(this.stages);

    for (StageGroup stageGroup : plan) {
      if (!stageGroup.parallel()) {
        currentInput = runSequential(stageGroup.stages(), currentInput);
      } else {
        currentInput = runParallel(stageGroup.stages(), currentInput);
      }
    }

    if (!(currentInput instanceof PipelineResponse response)) {
      throw new IllegalStateException(
          "Last stage did not produce a PipelineResponse. Got: "
              + currentInput.getClass().getName());
    }

    return response;
  }

  private StageInput runSequential(List<PipelineStage> stages, StageInput initialInput) {
    StageInput current = initialInput;
    for (PipelineStage stage : stages) {
      current = stage.process(current);
    }
    return current;
  }

  private StageOutput runParallel(List<PipelineStage> stages, StageInput inputForGroup)
      throws Exception {
    List<CompletableFuture<StageOutput>> futures =
        stages.stream()
            .map(
                stage ->
                    CompletableFuture.supplyAsync(() -> stage.process(inputForGroup), executor))
            .toList();

    CompletableFuture<Void> allDone =
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));

    CompletableFuture<List<StageOutput>> collected =
        allDone.thenApply(v -> futures.stream().map(CompletableFuture::join).toList());

    List<StageOutput> outputs;
    try {
      outputs = collected.get();
    } catch (ExecutionException e) {
      throw (e.getCause() instanceof Exception ex) ? ex : e;
    }

    return new ParallelOutputs(inputForGroup, outputs);
  }

  @Override
  public void close() {
    executor.shutdown();
  }
}
