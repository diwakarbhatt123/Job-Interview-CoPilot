package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.*;
import com.jobcopilot.parser.stages.PipelineStage;
import com.jobcopilot.profile_service.parser.model.output.*;
import com.jobcopilot.profile_service.parser.model.response.AnalysisPipelineResponse;

public class ExtractedResumeDataMerger implements PipelineStage {
  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof ParallelOutputs outputs)) {
      throw new IllegalArgumentException("Unexpected input for data merger.");
    }

    AnalysisPipelineResponse response = AnalysisPipelineResponse.builder().build();

    if (outputs.originalInput() instanceof SectionizedOutput sectionizedOutput) {
      response =
          response.toBuilder()
              .rawText(sectionizedOutput.rawText())
              .normalizedText(sectionizedOutput.normalizedText())
              .build();
    } else if (outputs.originalInput()
        instanceof NormalizedTextOutput(String normalizedText, String rawText)) {
      response = response.toBuilder().rawText(rawText).normalizedText(normalizedText).build();
    }

    for (StageOutput output : outputs.outputs()) {
      switch (output) {
        case EducationExtractedOutput educationExtractedOutput ->
            response = response.toBuilder().educations(educationExtractedOutput.entries()).build();
        case ExperienceExtractedOutput experienceExtractedOutput ->
            response =
                response.toBuilder().experiences(experienceExtractedOutput.entries()).build();
        case SkillExtractedOutput skillExtractedOutput ->
            response = response.toBuilder().skills(skillExtractedOutput.skills()).build();
        case YearsExtractedOutput yearsExtractedOutput ->
            response =
                response.toBuilder()
                    .yearsOfExperience(yearsExtractedOutput.yearsOfExperience())
                    .build();
        default -> {}
      }
    }

    return response;
  }
}
