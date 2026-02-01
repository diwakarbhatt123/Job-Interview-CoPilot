package com.jobcopilot.job_analyzer_service.parser.model.output;

import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.parser.model.output.StageOutput;
import java.util.List;

public record LabeledLinesOutput(
    String rawText, String normalizedText, List<LabeledLine> labeledLines) implements StageOutput {
  public LabeledLinesOutput {
    labeledLines = labeledLines == null ? List.of() : List.copyOf(labeledLines);
  }
}
