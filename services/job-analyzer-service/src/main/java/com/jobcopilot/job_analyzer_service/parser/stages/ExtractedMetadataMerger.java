package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.model.output.ExtractedMetadataOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.SkillExtractedOutput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;

public class ExtractedMetadataMerger implements PipelineStage {
  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof SkillExtractedOutput output)) {
      throw new IllegalArgumentException("Expected SkillExtractedOutput");
    }

    return new ExtractedMetadataOutput(
        output.rawText(),
        output.normalizedText(),
        output.seniority(),
        output.seniorityReason(),
        output.domain(),
        output.domainReason(),
        output.requiredSkills(),
        output.preferredSkills(),
        output.techStack());
  }
}
