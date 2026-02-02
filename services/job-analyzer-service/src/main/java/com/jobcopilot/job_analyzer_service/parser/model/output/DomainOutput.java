package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.parser.model.output.StageOutput;
import java.util.List;

public record DomainOutput(
    String rawText,
    String normalizedText,
    List<LabeledLine> labeledLines,
    SeniorityOutput seniorityOutput,
    Domain domain,
    String reason)
    implements StageOutput {
  public DomainOutput {
    labeledLines = labeledLines == null ? List.of() : List.copyOf(labeledLines);
  }
}
