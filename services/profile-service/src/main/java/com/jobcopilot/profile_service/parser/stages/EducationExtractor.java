package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class EducationExtractor implements PipelineStage {

  // Years: capture in reasonable range
  private static final Pattern YEAR = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b");
  private static final Pattern YEAR_RANGE =
      Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b\\s*[-–—]\\s*\\b(19\\d{2}|20\\d{2})\\b");

  // Degree-ish tokens (MVP list; extend later)
  private static final Pattern DEGREE_KEYWORDS =
      Pattern.compile(
          "(?i)\\b("
              + "b\\.?\\s*tech|b\\.?\\s*e\\.?|b\\.?\\s*sc|b\\.?\\s*a|bachelor(?:'s)?|undergraduate"
              + "|m\\.?\\s*tech|m\\.?\\s*sc|m\\.?\\s*a|mba|master(?:'s)?|postgraduate"
              + "|ph\\.?\\s*d|doctorate"
              + ")\\b");

  private static final Pattern FIELD_IN_PATTERN =
      Pattern.compile("(?i)\\b(?:in|major(?:ed)?\\s+in|speciali[sz]ation\\s+in)\\s+(.+)$");

  private static final Pattern INSTITUTION_HINT =
      Pattern.compile("(?i)\\b(university|college|institute|school|academy|polytechnic)\\b");

  private static final int MIN_YEAR = 1990;
  private static final int MAX_ENTRY_LINES = 6;

  @Override
  public StageOutput process(StageInput input) {
    if (!(input
        instanceof
        com.jobcopilot.profile_service.parser.model.output.SectionizedOutput sectionized)) {
      throw new IllegalArgumentException(
          "Invalid input type for EducationExtractor stage: " + input.getClass());
    }

    Optional<com.jobcopilot.profile_service.parser.model.output.SectionizedOutput.SectionDetail>
        eduSectionOpt =
            sectionized.sections().stream()
                .filter(
                    s ->
                        s.getSection()
                            == com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection
                                .EDUCATION)
                .findFirst();

    if (eduSectionOpt.isEmpty()) {
      return new com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput(
          List.of());
    }

    List<String> lines =
        eduSectionOpt.get().getLines() == null ? List.of() : eduSectionOpt.get().getLines();

    List<com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput.EducationEntry>
        entries = extractEntries(lines);

    return new com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput(entries);
  }

  @Override
  public boolean isParallelizable() {
    return true;
  }

  private List<
          com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
              .EducationEntry>
      extractEntries(List<String> lines) {
    List<com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput.EducationEntry>
        out = new ArrayList<>();

    com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
            .EducationEntryBuilder
        current = null;

    for (String raw : lines) {
      if (raw == null) continue;

      String line = raw.trim();

      if (line.isEmpty()) {
        if (current != null && current.hasMeaningfulContent()) {
          out.add(current.build());
          current = null;
        }
        continue;
      }

      if (current == null) {
        current =
            new com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
                .EducationEntryBuilder();
      }

      if (current.lines.size() < MAX_ENTRY_LINES) {
        current.lines.add(line);
      }
    }

    if (current != null && current.hasMeaningfulContent()) {
      out.add(current.build());
    }

    // If we still have nothing but there was content, return a single coarse entry
    if (out.isEmpty()) {
      com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
              .EducationEntryBuilder
          fallback =
              new com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
                  .EducationEntryBuilder();
      for (String l : lines) {
        if (l == null) continue;
        String t = l.trim();
        if (t.isEmpty()) continue;
        if (fallback.lines.size() < MAX_ENTRY_LINES) fallback.lines.add(t);
      }
      if (fallback.hasMeaningfulContent()) out.add(fallback.build());
    }

    // Post-process entries: parse fields from each entry's lines
    return out.stream().map(this::enrich).collect(Collectors.toList());
  }

  private com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput.EducationEntry
      enrich(
          com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput.EducationEntry
              entry) {
    List<String> lines = entry.lines() == null ? List.of() : entry.lines();

    // Extract years (prefer range; else single years -> treat as endYear)
    Optional<int[]> range = extractYearRange(lines);
    Integer startYear = null;
    Integer endYear = null;

    if (range.isPresent()) {
      startYear = range.get()[0];
      endYear = range.get()[1];
    } else {
      List<Integer> years = extractYears(lines);
      if (!years.isEmpty()) {
        // Education often lists graduation year as a single year; choose max as endYear
        endYear = years.stream().max(Comparator.naturalOrder()).orElse(null);
      }
    }

    // Degree + field + institution
    String combined = String.join(" | ", lines);
    Optional<String> degree = extractDegree(combined);
    Optional<String> field = extractField(combined, degree.orElse(null));
    Optional<String> institution =
        extractInstitution(lines, degree.orElse(null), field.orElse(null));

    return new com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput
        .EducationEntry(
        institution.orElse(null),
        degree.orElse(null),
        field.orElse(null),
        startYear,
        endYear,
        List.copyOf(lines));
  }

  private Optional<int[]> extractYearRange(List<String> lines) {
    int currentYear = LocalDate.now().getYear();
    for (String l : lines) {
      var m = YEAR_RANGE.matcher(l);
      if (m.find()) {
        Integer start = safeParseYear(m.group(1), currentYear).orElse(null);
        Integer end = safeParseYear(m.group(2), currentYear).orElse(null);
        if (start != null && end != null && start <= end) {
          return Optional.of(new int[] {start, end});
        }
      }
    }
    return Optional.empty();
  }

  private List<Integer> extractYears(List<String> lines) {
    int currentYear = LocalDate.now().getYear();
    return lines.stream()
        .flatMap(l -> YEAR.matcher(l).results().map(r -> r.group(1)))
        .map(s -> safeParseYear(s, currentYear))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
  }

  private Optional<Integer> safeParseYear(String s, int currentYear) {
    try {
      int y = Integer.parseInt(s);
      if (y < MIN_YEAR || y > currentYear) return Optional.empty();
      return Optional.of(y);
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private Optional<String> extractDegree(String text) {
    var m = DEGREE_KEYWORDS.matcher(text);
    if (m.find()) {
      return Optional.of(m.group(1).trim());
    }
    return Optional.empty();
  }

  private Optional<String> extractField(String text, String degree) {
    // First try "in <field>" pattern
    var m = FIELD_IN_PATTERN.matcher(text);
    if (m.find()) {
      String f = cleanupField(m.group(1));
      if (!f.isBlank()) return Optional.of(f);
    }

    // Heuristic: if degree exists and line contains it, take the rest of that line after degree
    if (degree != null) {
      String lower = text.toLowerCase(Locale.ROOT);
      int idx = lower.indexOf(degree.toLowerCase(Locale.ROOT));
      if (idx >= 0) {
        String after = text.substring(idx + degree.length()).trim();
        after = after.replaceAll("^[,\\-–—|:]+\\s*", "");
        after = cleanupField(after);
        if (!after.isBlank() && after.length() <= 120) return Optional.of(after);
      }
    }

    return Optional.empty();
  }

  private String cleanupField(String s) {
    String out = s.trim();
    // stop at obvious separators
    out = out.split("\\(|\\)|\\||—|–|-|\\d{4}")[0].trim();
    // remove trailing punctuation
    out = out.replaceAll("[,;:.]+$", "").trim();
    return out;
  }

  private Optional<String> extractInstitution(List<String> lines, String degree, String field) {
    // Prefer lines containing institution hints
    for (String l : lines) {
      if (INSTITUTION_HINT.matcher(l).find()) {
        String inst = stripNoise(l, degree, field);
        if (!inst.isBlank()) return Optional.of(inst);
      }
    }

    // Fallback: choose the most title-ish line that is not obviously degree/field
    for (String l : lines) {
      String cand = stripNoise(l, degree, field);
      if (cand.isBlank()) continue;
      if (DEGREE_KEYWORDS.matcher(cand).find()) continue;
      if (looksTitleish(cand) && cand.length() <= 140) return Optional.of(cand);
    }

    return Optional.empty();
  }

  private String stripNoise(String line, String degree, String field) {
    String s = line.trim();
    s = s.replaceAll("\\b(19\\d{2}|20\\d{2})\\b", "").trim();
    if (degree != null) s = s.replace(degree, "").trim();
    if (field != null && !field.isBlank()) s = s.replace(field, "").trim();
    s = s.replaceAll("[()\\[\\],;:|]+", " ").replaceAll("\\s{2,}", " ").trim();
    return s;
  }

  private boolean looksTitleish(String line) {
    String t = line.trim();
    if (t.isEmpty()) return false;

    String[] words = t.split("\\s+");
    int considered = 0;
    int caps = 0;

    for (String w : words) {
      if (w.isEmpty()) continue;
      String cleaned = w.replaceAll("^[^\\p{L}]+|[^\\p{L}]+$", "");
      if (cleaned.isEmpty()) continue;

      considered++;
      char first = cleaned.charAt(0);
      if (Character.isUpperCase(first)) caps++;
    }

    return considered > 0 && caps >= Math.ceil(considered * 0.6);
  }
}
