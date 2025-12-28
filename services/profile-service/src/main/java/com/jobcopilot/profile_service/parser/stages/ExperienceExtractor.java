package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.profile_service.parser.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.ExperienceExtractedOutput;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;

public class ExperienceExtractor implements PipelineStage {

  private static final Pattern MONTH_YEAR_RANGE =
      Pattern.compile(
          "(?i)\\b(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+(19\\d{2}|20\\d{2})\\b\\s*[-–—]\\s*(present|current|now|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+(19\\d{2}|20\\d{2}))");

  private static final Pattern YEAR_YEAR_RANGE =
      Pattern.compile(
          "(?i)\\b(19\\d{2}|20\\d{2})\\b\\s*[-–—]\\s*\\b(19\\d{2}|20\\d{2}|present|current|now)\\b");

  private static final Pattern HEADER_SEPARATORS =
      Pattern.compile("\\s*(\\||—|–|-|@|\\bat\\b)\\s*", Pattern.CASE_INSENSITIVE);

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof SectionizedOutput sectionized)) {
      throw new IllegalArgumentException(
          "Invalid input type for ExperienceExtractor stage: " + input.getClass());
    }

    Optional<SectionizedOutput.SectionDetail> expSectionOpt =
        sectionized.sections().stream()
            .filter(s -> s.getSection() == ResumeSection.EXPERIENCE)
            .findFirst();

    if (expSectionOpt.isEmpty()) {
      return new ExperienceExtractedOutput(List.of());
    }

    List<String> lines = expSectionOpt.get().getLines();
    List<ExperienceExtractedOutput.ExperienceEntry> entries = extractEntries(lines);

    return new ExperienceExtractedOutput(entries);
  }

  @Override
  public boolean isParallelizable() {
    return true;
  }

  private List<ExperienceExtractedOutput.ExperienceEntry> extractEntries(List<String> lines) {
    List<ExperienceExtractedOutput.ExperienceEntry> out = new ArrayList<>();

    ExperienceExtractedOutput.ExperienceEntryBuilder current = null;

    for (String raw : lines) {
      if (raw == null) continue;

      String line = raw.trim();
      if (line.isEmpty()) continue;

      boolean isHeader = isLikelyExperienceHeader(line);

      if (isHeader) {
        if (current != null && current.hasMeaningfulContent()) {
          out.add(current.build());
        }
        current = new ExperienceExtractedOutput.ExperienceEntryBuilder();
        current.headerLine = line;

        HeaderParse hp = parseHeader(line);
        current.company = hp.company.orElse(null);
        current.role = hp.role.orElse(null);
        current.startYear = hp.startYear.orElse(null);
        current.endYear = hp.endYear.orElse(null);
        current.isCurrent = hp.isCurrent;

        continue;
      }

      if (current != null) {
        if (isBulletLine(line)) {
          current.details.add(stripBullet(line));
        } else {
          if (line.length() <= 200) {
            current.details.add(line);
          }
        }
      }
    }

    if (current != null && current.hasMeaningfulContent()) {
      out.add(current.build());
    }

    if (out.isEmpty()) {
      ExperienceExtractedOutput.ExperienceEntryBuilder fallback =
          new ExperienceExtractedOutput.ExperienceEntryBuilder();
      fallback.headerLine = null;
      for (String l : lines) {
        if (l == null) continue;
        String t = l.trim();
        if (t.isEmpty()) continue;
        if (isBulletLine(t)) fallback.details.add(stripBullet(t));
        else if (t.length() <= 200) fallback.details.add(t);
      }
      if (fallback.hasMeaningfulContent()) out.add(fallback.build());
    }

    return out;
  }

  private boolean isLikelyExperienceHeader(String line) {
    if (line == null) return false;
    String t = line.trim();
    if (t.isEmpty()) return false;
    if (isBulletLine(t)) return false;

    if (containsDateRange(t)) return true;

    return HEADER_SEPARATORS.matcher(t).find() && looksTitleish(t) && t.length() <= 140;
  }

  private boolean containsDateRange(String line) {
    return MONTH_YEAR_RANGE.matcher(line).find() || YEAR_YEAR_RANGE.matcher(line).find();
  }

  private boolean isBulletLine(String line) {
    String t = line.trim();
    return t.startsWith("-")
        || t.startsWith("•")
        || t.startsWith("▪")
        || t.startsWith("*")
        || t.startsWith("–")
        || t.startsWith("—");
  }

  private String stripBullet(String line) {
    String t = line.trim();
    return t.replaceFirst("^(?:[-•▪*–—]\\s*)+", "").trim();
  }

  private boolean looksTitleish(String line) {
    String t = line.trim();
    if (t.isEmpty()) return false;

    String lower = t.toLowerCase(Locale.ROOT);
    if (lower.contains("engineer")
        || lower.contains("developer")
        || lower.contains("analyst")
        || lower.contains("manager")
        || lower.contains("consultant")
        || lower.contains("lead")) {
      return true;
    }

    String[] words = t.split("\\s+");
    int considered = 0;
    int caps = 0;
    for (String w : words) {
      if (w.isEmpty()) continue;
      if (w.length() == 1) continue;

      String cleaned = w.replaceAll("^[^\\p{L}]+|[^\\p{L}]+$", "");
      if (cleaned.isEmpty()) continue;

      considered++;
      char first = cleaned.charAt(0);
      if (Character.isUpperCase(first)) caps++;
    }

    return considered > 0 && caps >= Math.ceil(considered * 0.6);
  }

  private HeaderParse parseHeader(String headerLine) {
    DateParse dp = parseDates(headerLine);

    String noDates = headerLine;
    // Strip date ranges so they don't get treated as company/role tokens.
    noDates = MONTH_YEAR_RANGE.matcher(noDates).replaceAll("");
    noDates = YEAR_YEAR_RANGE.matcher(noDates).replaceAll("");
    String[] parts = HEADER_SEPARATORS.split(noDates);
    List<String> tokens = new ArrayList<>();
    for (String p : parts) {
      String t = p.trim();
      if (!t.isEmpty()) tokens.add(t);
    }

    Optional<String> company = Optional.empty();
    Optional<String> role = Optional.empty();

    if (tokens.size() >= 2) {
      Optional<String> roleToken = tokens.stream().filter(this::containsRoleKeyword).findFirst();
      if (roleToken.isPresent()) {
        role = roleToken;
        company = tokens.stream().filter(t -> !t.equals(roleToken.get())).findFirst();
      } else {
        company = Optional.of(tokens.get(0));
        role = Optional.of(tokens.get(1));
      }
    } else if (tokens.size() == 1) {
      if (containsRoleKeyword(tokens.getFirst())) role = Optional.of(tokens.getFirst());
      else company = Optional.of(tokens.getFirst());
    }

    return new HeaderParse(company, role, dp.startYear, dp.endYear, dp.isCurrent);
  }

  private boolean containsRoleKeyword(String s) {
    String lower = s.toLowerCase(Locale.ROOT);
    return lower.contains("engineer")
        || lower.contains("developer")
        || lower.contains("analyst")
        || lower.contains("manager")
        || lower.contains("consultant")
        || lower.contains("architect")
        || lower.contains("lead")
        || lower.contains("director")
        || lower.contains("intern");
  }

  private DateParse parseDates(String line) {
    var m1 = MONTH_YEAR_RANGE.matcher(line);
    if (m1.find()) {
      Integer start = safeInt(m1.group(1)).orElse(null);
      String endToken = m1.group(2);
      boolean isCurrent = isCurrentToken(endToken);

      Integer end = null;
      if (!isCurrent) {
        end = safeInt(m1.group(3)).orElse(null);
      }

      return new DateParse(Optional.ofNullable(start), Optional.ofNullable(end), isCurrent);
    }

    var m2 = YEAR_YEAR_RANGE.matcher(line);
    if (m2.find()) {
      Integer start = safeInt(m2.group(1)).orElse(null);
      String endToken = m2.group(2);
      boolean isCurrent = isCurrentToken(endToken);

      Integer end = null;
      if (!isCurrent) end = safeInt(endToken).orElse(null);

      return new DateParse(Optional.ofNullable(start), Optional.ofNullable(end), isCurrent);
    }

    return new DateParse(Optional.empty(), Optional.empty(), false);
  }

  private boolean isCurrentToken(String token) {
    if (token == null) return false;
    String t = token.toLowerCase(Locale.ROOT).trim();
    return t.equals("present") || t.equals("current") || t.equals("now");
  }

  private Optional<Integer> safeInt(String s) {
    try {
      if (s == null) return Optional.empty();
      return Optional.of(Integer.parseInt(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  private record HeaderParse(
      Optional<String> company,
      Optional<String> role,
      Optional<Integer> startYear,
      Optional<Integer> endYear,
      boolean isCurrent) {}

  private record DateParse(
      Optional<Integer> startYear, Optional<Integer> endYear, boolean isCurrent) {}
}
