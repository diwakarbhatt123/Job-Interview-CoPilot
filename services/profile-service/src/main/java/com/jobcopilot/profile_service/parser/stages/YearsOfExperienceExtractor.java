package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.profile_service.parser.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;
import com.jobcopilot.profile_service.parser.model.output.YearsExtractedOutput;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class YearsOfExperienceExtractor implements PipelineStage {

  public static final Pattern YEARS_OF_EXP_EXPERIENCE_MONTH_YEAR_REGEX =
      Pattern.compile(
          "(?i)\\b(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+(19\\d{2}|20\\d{2})\\b\\s*[-–—]\\s*(?:present|current|now|(?:jan|feb|mar|apr|may|jun|jul|aug|sep|sept|oct|nov|dec)\\s+(?:19\\d{2}|20\\d{2}))");
  private static final Pattern YEARS_OF_EXP_SUMMARY_REGEX =
      Pattern.compile("(?i)\\b(\\d{1,2})\\s*\\+?\\s*(years?|yrs?)\\s+(of\\s+)?experience\\b");
  private static final Pattern YEARS_OF_EXP_EXPERIENCE_YEAR_YEAR_REGEX =
      Pattern.compile(
          "(?i)\\b(19\\d{2}|20\\d{2})\\b\\s*[-–—]\\s*\\b(19\\d{2}|20\\d{2}|present|current|now)\\b");

  // Guardrails
  private static final int MIN_PLAUSIBLE_START_YEAR = 1990;
  private static final int MAX_PLAUSIBLE_YEARS_OF_EXPERIENCE = 50;

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof SectionizedOutput sectionizedOutput)) {
      throw new IllegalArgumentException("Input must be of type SectionizedOutput");
    }

    // 1) SUMMARY first (highest confidence)
    Optional<SectionizedOutput.SectionDetail> summarySection =
        sectionizedOutput.sections().stream()
            .filter(sectionDetail -> ResumeSection.SUMMARY == sectionDetail.getSection())
            .findFirst();

    if (summarySection.isPresent()) {
      Optional<Integer> yearsFromSummary =
          extractYearsOfExperienceFromSummary(summarySection.get());
      if (yearsFromSummary.isPresent()) {
        return new YearsExtractedOutput(yearsFromSummary.get());
      }
    }

    // 1b) Fallback: scan full normalized text for explicit "years of experience"
    Optional<Integer> yearsFromWholeText =
        extractYearsOfExperienceFromText(sectionizedOutput.normalizedText());
    if (yearsFromWholeText.isPresent()) {
      return new YearsExtractedOutput(yearsFromWholeText.get());
    }

    // 2) EXPERIENCE fallback (date inference)
    Optional<SectionizedOutput.SectionDetail> experienceSection =
        sectionizedOutput.sections().stream()
            .filter(sectionDetail -> ResumeSection.EXPERIENCE == sectionDetail.getSection())
            .findFirst();

    if (experienceSection.isPresent()) {
      Optional<Integer> yearsFromExperience =
          extractYearsOfExperienceFromExperience(experienceSection.get());
      if (yearsFromExperience.isPresent()) {
        return new YearsExtractedOutput(yearsFromExperience.get());
      }
    }

    return new YearsExtractedOutput(null);
  }

  @Override
  public boolean isParallelizable() {
    return true;
  }

  private Optional<Integer> extractYearsOfExperienceFromSummary(
      SectionizedOutput.SectionDetail section) {
    String summaryText = String.join("\n", section.getLines());
    return extractYearsOfExperienceFromText(summaryText);
  }

  private Optional<Integer> extractYearsOfExperienceFromText(String text) {
    if (text == null || text.isBlank()) return Optional.empty();

    // Collect all matches and take the maximum (conservative vs undercount)
    Set<Integer> matches =
        YEARS_OF_EXP_SUMMARY_REGEX
            .matcher(text)
            .results()
            .map(r -> r.group(1))
            .map(this::safeParseInt)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toSet());

    return matches.stream().max(Comparator.naturalOrder()).map(this::clampYearsOfExperience);
  }

  private Optional<Integer> extractYearsOfExperienceFromExperience(
      SectionizedOutput.SectionDetail section) {
    String experienceText = String.join("\n", section.getLines());

    int currentYear = LocalDate.now().getYear();

    // Month-Year ranges: extract captured start year in group(1)
    Set<Integer> startYearsFromMonthYear =
        YEARS_OF_EXP_EXPERIENCE_MONTH_YEAR_REGEX
            .matcher(experienceText)
            .results()
            .map(r -> r.group(1))
            .map(this::safeParseInt)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(y -> isPlausibleStartYear(y, currentYear))
            .collect(Collectors.toSet());

    // Year-Year / Year-Present ranges: extract captured start year in group(1)
    Set<Integer> startYearsFromYearYear =
        YEARS_OF_EXP_EXPERIENCE_YEAR_YEAR_REGEX
            .matcher(experienceText)
            .results()
            .map(r -> r.group(1))
            .map(this::safeParseInt)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(y -> isPlausibleStartYear(y, currentYear))
            .collect(Collectors.toSet());

    startYearsFromMonthYear.addAll(startYearsFromYearYear);

    // Infer years = currentYear - earliestStartYear
    return startYearsFromMonthYear.stream()
        .min(Comparator.naturalOrder())
        .map(startYear -> currentYear - startYear)
        .filter(y -> y >= 0)
        .map(this::clampYearsOfExperience);
  }

  private boolean isPlausibleStartYear(int year, int currentYear) {
    return year >= MIN_PLAUSIBLE_START_YEAR && year <= currentYear;
  }

  private int clampYearsOfExperience(int years) {
    if (years < 0) return 0;
    return Math.min(years, MAX_PLAUSIBLE_YEARS_OF_EXPERIENCE);
  }

  private Optional<Integer> safeParseInt(String s) {
    try {
      return Optional.of(Integer.parseInt(s));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
