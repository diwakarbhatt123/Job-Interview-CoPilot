package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.parser.model.output.StageOutput;
import java.util.List;

public record SeniorityOutput(
    String rawText,
    String normalizedText,
    List<LabeledLine> labeledLines,
    Seniority seniority,
    String reason)
    implements StageOutput {
  public SeniorityOutput {
    labeledLines = labeledLines == null ? List.of() : List.copyOf(labeledLines);
  }
}
