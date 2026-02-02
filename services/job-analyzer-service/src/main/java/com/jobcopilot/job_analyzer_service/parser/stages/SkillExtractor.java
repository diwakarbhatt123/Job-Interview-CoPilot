package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.dictionary.BlockLabel;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Skill;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.job_analyzer_service.parser.model.SkillExtractionResult;
import com.jobcopilot.job_analyzer_service.parser.model.output.DomainOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.SeniorityOutput;
import com.jobcopilot.job_analyzer_service.parser.model.output.SkillExtractedOutput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class SkillExtractor implements PipelineStage {
  private static final List<String> REQUIRED_CUES =
      List.of(
          "must have",
          "required",
          "we require",
          "you have",
          "strong experience in",
          "proficient in");
  private static final List<String> PREFERRED_CUES =
      List.of("nice to have", "preferred", "bonus", "plus", "good to have", "would be a plus");

  private static final Map<String, String> ALIAS_TO_CANONICAL = buildAliasToCanonical();
  private static final Map<String, Pattern> ALIAS_PATTERNS = buildAliasPatterns();

  private static Map<String, Pattern> buildAliasPatterns() {
    Map<String, Pattern> patterns = new LinkedHashMap<>();
    List<String> aliases = new ArrayList<>(ALIAS_TO_CANONICAL.keySet());
    aliases.sort(Comparator.comparingInt(String::length).reversed());
    for (String alias : aliases) {
      patterns.put(alias, Pattern.compile(buildAliasRegex(alias), Pattern.CASE_INSENSITIVE));
    }
    return patterns;
  }

  private static Map<String, String> buildAliasToCanonical() {
    Map<String, String> map = new LinkedHashMap<>();
    for (Skill skill : Skill.values()) {
      for (String alias : skill.aliases()) {
        map.put(alias, skill.name());
      }
    }
    return map;
  }

  private static String buildAliasRegex(String alias) {
    String[] parts = alias.split("\\s+");
    if (parts.length == 1) {
      return "\\b" + Pattern.quote(alias) + "\\b";
    }
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

  @Override
  public StageOutput process(StageInput input) {
    if (!(input
        instanceof
        DomainOutput(
            String rawText,
            String normalizedText,
            List<LabeledLine> labeledLines,
            SeniorityOutput seniorityOutput,
            Domain domain,
            String reason))) {
      throw new IllegalArgumentException("Expected DomainOutput");
    }

    SkillExtractionResult skills = extract(labeledLines);
    return new SkillExtractedOutput(
        rawText,
        normalizedText,
        labeledLines,
        seniorityOutput.seniority(),
        seniorityOutput.reason(),
        domain,
        reason,
        skills.requiredSkills(),
        skills.preferredSkills(),
        skills.techStack());
  }

  public SkillExtractionResult extract(List<LabeledLine> labeledLines) {
    Set<String> required = new TreeSet<>();
    Set<String> preferred = new TreeSet<>();
    Set<String> techStack = new TreeSet<>();

    for (LabeledLine labeledLine : labeledLines) {
      String line = labeledLine.lineText();
      if (line == null || line.isBlank()) {
        continue;
      }

      Bucket bucket = classifyLine(labeledLine);
      Set<String> found = findSkills(line);

      if (bucket == Bucket.REQUIRED) {
        required.addAll(found);
      } else if (bucket == Bucket.PREFERRED) {
        preferred.addAll(found);
      } else {
        techStack.addAll(found);
      }
    }

    techStack.addAll(required);
    techStack.addAll(preferred);

    return new SkillExtractionResult(
        List.copyOf(required), List.copyOf(preferred), List.copyOf(techStack));
  }

  private Bucket classifyLine(LabeledLine labeledLine) {
    String line = labeledLine.lineText().toLowerCase(Locale.ROOT);
    if (containsCue(line, REQUIRED_CUES)) {
      return Bucket.REQUIRED;
    }
    if (containsCue(line, PREFERRED_CUES)) {
      return Bucket.PREFERRED;
    }

    if (labeledLine.label() == BlockLabel.PREFERRED) {
      return Bucket.PREFERRED;
    }
    if (labeledLine.label() == BlockLabel.REQUIREMENTS) {
      return Bucket.REQUIRED;
    }

    // For OTHER/RESPONSIBILITIES we treat skills as tech stack only.
    return Bucket.TECH_STACK;
  }

  private boolean containsCue(String line, List<String> cues) {
    for (String cue : cues) {
      if (line.contains(cue)) {
        return true;
      }
    }
    return false;
  }

  private Set<String> findSkills(String line) {
    Set<String> matches = new TreeSet<>();
    for (Map.Entry<String, Pattern> entry : ALIAS_PATTERNS.entrySet()) {
      if (entry.getValue().matcher(line).find()) {
        matches.add(ALIAS_TO_CANONICAL.get(entry.getKey()));
      }
    }
    if (matches.contains("SPRING_BOOT")) {
      matches.remove("SPRING");
    }
    return matches;
  }

  private enum Bucket {
    REQUIRED,
    PREFERRED,
    TECH_STACK
  }
}
