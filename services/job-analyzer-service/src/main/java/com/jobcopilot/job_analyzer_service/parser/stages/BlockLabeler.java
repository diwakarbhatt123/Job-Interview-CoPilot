package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.dictionary.BlockLabel;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.job_analyzer_service.parser.model.output.LabeledLinesOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.NormalizedJdTextOutput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlockLabeler implements PipelineStage {

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof NormalizedJdTextOutput(String rawText, String normalizedText))) {
      throw new IllegalArgumentException("Expected NormalizedJdTextOutput");
    }
    List<LabeledLine> labeled = labelLines(normalizedText);
    return new LabeledLinesOutput(rawText, normalizedText, labeled);
  }

  public List<LabeledLine> labelLines(String normalizedText) {
    List<LabeledLine> labeled = new ArrayList<>();
    if (normalizedText == null || normalizedText.isBlank()) {
      return labeled;
    }

    BlockLabel current = BlockLabel.OTHER;
    String[] lines = normalizedText.split("\n", -1);
    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      BlockLabel header = detectHeader(line);
      if (header != null) {
        current = header;
        labeled.add(new LabeledLine(i, line, BlockLabel.OTHER));
      } else {
        labeled.add(new LabeledLine(i, line, current));
      }
    }
    return labeled;
  }

  private BlockLabel detectHeader(String line) {
    if (line == null) {
      return null;
    }
    String cleaned = line.trim();
    if (cleaned.isEmpty()) {
      return null;
    }
    if (cleaned.startsWith("-")) {
      return null;
    }
    if (cleaned.endsWith(":")) {
      cleaned = cleaned.substring(0, cleaned.length() - 1).trim();
    }
    if (!isHeaderLike(cleaned)) {
      return null;
    }
    String lower = cleaned.toLowerCase(Locale.ROOT);

    if (containsAny(
        lower, "requirements", "qualifications", "what you'll need", "what you will need")) {
      return BlockLabel.REQUIREMENTS;
    }
    if (containsAny(
        lower,
        "preferred",
        "nice to have",
        "nice-to-have",
        "bonus",
        "good to have",
        "would be a plus")) {
      return BlockLabel.PREFERRED;
    }
    if (containsAny(lower, "responsibilities", "what you'll do", "what you will do", "duties")) {
      return BlockLabel.RESPONSIBILITIES;
    }
    return null;
  }

  private boolean isHeaderLike(String cleaned) {
    if (cleaned.length() > 60) {
      return false;
    }
    int words = cleaned.split("\\s+").length;
    return words <= 6;
  }

  private boolean containsAny(String value, String... candidates) {
    for (String candidate : candidates) {
      if (value.contains(candidate)) {
        return true;
      }
    }
    return false;
  }
}
