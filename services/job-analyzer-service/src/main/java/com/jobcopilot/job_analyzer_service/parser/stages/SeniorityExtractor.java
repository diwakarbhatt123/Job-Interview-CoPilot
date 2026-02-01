package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.SeniorityResult;
import com.jobcopilot.job_analyzer_service.parser.model.output.LabeledLinesOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.SeniorityOutput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SeniorityExtractor implements PipelineStage {
  private static final Map<Seniority, List<Pattern>> PATTERNS = buildPatterns();

  private static Map<Seniority, List<Pattern>> buildPatterns() {
    Map<Seniority, List<Pattern>> map = new LinkedHashMap<>();
    for (Seniority seniority : Seniority.orderedByPrecedence()) {
      List<Pattern> patterns = new ArrayList<>();
      for (String alias : seniority.aliases()) {
        patterns.add(wordPattern(alias));
      }
      map.put(seniority, patterns);
    }
    return map;
  }

  private static Pattern wordPattern(String token) {
    return Pattern.compile("\\b" + Pattern.quote(token) + "\\b", Pattern.CASE_INSENSITIVE);
  }

  @Override
  public StageOutput process(StageInput input) {
    if (!(input
        instanceof
        LabeledLinesOutput(
            String rawText,
            String normalizedText,
            List<com.jobcopilot.job_analyzer_service.parser.model.LabeledLine> labeledLines))) {
      throw new IllegalArgumentException("Expected LabeledLinesOutput");
    }

    SeniorityResult result = extract(normalizedText);
    return new SeniorityOutput(
        rawText, normalizedText, labeledLines, result.seniority(), result.reason());
  }

  public SeniorityResult extract(String normalizedText) {
    if (normalizedText == null || normalizedText.isBlank()) {
      return new SeniorityResult(Seniority.UNKNOWN, "empty text");
    }

    List<String> lines = collectTitleLines(normalizedText);
    SeniorityResult fromTitle = matchLines(lines, "title");
    if (fromTitle.seniority() != Seniority.UNKNOWN) {
      return fromTitle;
    }

    SeniorityResult fromBody = matchLines(List.of(normalizedText), "body");
    if (fromBody.seniority() != Seniority.UNKNOWN) {
      return fromBody;
    }

    return new SeniorityResult(Seniority.UNKNOWN, "no seniority match");
  }

  private SeniorityResult matchLines(List<String> lines, String source) {
    for (Map.Entry<Seniority, List<Pattern>> entry : PATTERNS.entrySet()) {
      for (String line : lines) {
        for (Pattern pattern : entry.getValue()) {
          if (pattern.matcher(line).find()) {
            return new SeniorityResult(
                entry.getKey(), "matched '" + pattern.pattern() + "' in " + source);
          }
        }
      }
    }
    return new SeniorityResult(Seniority.UNKNOWN, "no match in " + source);
  }

  private List<String> collectTitleLines(String normalizedText) {
    List<String> lines = new ArrayList<>();
    String[] all = normalizedText.split("\n", -1);
    for (String line : all) {
      String trimmed = line.trim();
      if (trimmed.isEmpty()) {
        if (!lines.isEmpty()) {
          break;
        }
        continue;
      }
      lines.add(trimmed);
      if (lines.size() >= 3) {
        break;
      }
    }
    return lines.isEmpty() ? List.of(normalizedText) : lines;
  }
}
