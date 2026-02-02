package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.model.DomainResult;
import com.jobcopilot.job_analyzer_service.parser.model.output.DomainOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.SeniorityOutput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class DomainExtractor implements PipelineStage {
  private static final Map<Domain, List<Pattern>> KEYWORD_PATTERNS = buildKeywordPatterns();
  private static final int MIN_SCORE = 2;

  private static Map<Domain, List<Pattern>> buildKeywordPatterns() {
    Map<Domain, List<Pattern>> map = new LinkedHashMap<>();
    for (Domain domain : Domain.values()) {
      List<Pattern> patterns = new ArrayList<>();
      for (String keyword : domain.keywords()) {
        patterns.add(Pattern.compile(buildKeywordRegex(keyword), Pattern.CASE_INSENSITIVE));
      }
      map.put(domain, patterns);
    }
    return map;
  }

  private static String buildKeywordRegex(String keyword) {
    String trimmed = keyword.trim().toLowerCase(Locale.ROOT);
    if (trimmed.contains(" ")) {
      String[] parts = trimmed.split("\\s+");
      StringBuilder builder = new StringBuilder();
      builder.append("\\b");
      for (int i = 0; i < parts.length; i++) {
        if (i > 0) {
          builder.append("\\s+");
        }
        builder.append(Pattern.quote(parts[i]));
      }
      builder.append("\\b");
      return builder.toString();
    }
    return "\\b" + Pattern.quote(trimmed) + "\\b";
  }

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof SeniorityOutput seniorityOutput)) {
      throw new IllegalArgumentException("Expected SeniorityOutput");
    }
    DomainResult result = extract(seniorityOutput.normalizedText());
    return new DomainOutput(
        seniorityOutput.rawText(),
        seniorityOutput.normalizedText(),
        seniorityOutput.labeledLines(),
        seniorityOutput,
        result.domain(),
        result.reason());
  }

  public DomainResult extract(String normalizedText) {
    if (normalizedText == null || normalizedText.isBlank()) {
      return new DomainResult(Domain.UNKNOWN, "empty text");
    }

    String text = normalizedText.toLowerCase(Locale.ROOT);
    String title = firstNonEmptyLine(normalizedText).toLowerCase(Locale.ROOT);
    Map<Domain, Integer> scores = new LinkedHashMap<>();
    Map<Domain, List<String>> matches = new LinkedHashMap<>();

    for (Map.Entry<Domain, List<Pattern>> entry : KEYWORD_PATTERNS.entrySet()) {
      int score = 0;
      List<String> matched = new ArrayList<>();
      for (Pattern keyword : entry.getValue()) {
        if (keyword.matcher(text).find()) {
          score++;
          matched.add(keyword.pattern());
        }
        if (!title.isEmpty() && keyword.matcher(title).find()) {
          score += 2;
          matched.add("title:" + keyword.pattern());
        }
      }
      scores.put(entry.getKey(), score);
      matches.put(entry.getKey(), matched);
    }

    Domain best = Domain.UNKNOWN;
    int bestScore = 0;
    int secondScore = 0;
    Domain bestTitle = Domain.UNKNOWN;
    int bestTitleScore = 0;
    for (Map.Entry<Domain, Integer> entry : scores.entrySet()) {
      int score = entry.getValue();
      if (score > bestScore) {
        secondScore = bestScore;
        bestScore = score;
        best = entry.getKey();
      } else if (score > secondScore) {
        secondScore = score;
      }
      int titleScore =
          (int)
              matches.get(entry.getKey()).stream()
                  .filter(match -> match.startsWith("title:"))
                  .count();
      if (titleScore > bestTitleScore) {
        bestTitleScore = titleScore;
        bestTitle = entry.getKey();
      }
    }

    if (title.contains("full stack") || title.contains("fullstack")) {
      return new DomainResult(Domain.FULLSTACK, "title:fullstack");
    }

    if (bestTitleScore > 0) {
      return new DomainResult(bestTitle, "title signals: " + matches.get(bestTitle));
    }

    if (bestScore < MIN_SCORE || bestScore == secondScore) {
      return new DomainResult(Domain.UNKNOWN, "weak or tied domain signal");
    }

    return new DomainResult(best, "matched: " + matches.get(best));
  }

  private String firstNonEmptyLine(String text) {
    for (String line : text.split("\n", -1)) {
      String trimmed = line.trim();
      if (!trimmed.isEmpty()) {
        return trimmed;
      }
    }
    return "";
  }
}
