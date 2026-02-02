package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.parser.model.output.StageOutput;
import java.util.List;

public record SkillExtractedOutput(
    String rawText,
    String normalizedText,
    List<LabeledLine> labeledLines,
    Seniority seniority,
    String seniorityReason,
    Domain domain,
    String domainReason,
    List<String> requiredSkills,
    List<String> preferredSkills,
    List<String> techStack)
    implements StageOutput {
  public SkillExtractedOutput {
    labeledLines = labeledLines == null ? List.of() : List.copyOf(labeledLines);
    requiredSkills = requiredSkills == null ? List.of() : List.copyOf(requiredSkills);
    preferredSkills = preferredSkills == null ? List.of() : List.copyOf(preferredSkills);
    techStack = techStack == null ? List.of() : List.copyOf(techStack);
  }
}
