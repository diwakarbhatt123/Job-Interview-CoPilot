package com.jobcopilot.parser.model.output;

import java.util.ArrayList;
import java.util.List;

public record ExperienceExtractedOutput(List<ExperienceEntry> entries) implements StageOutput {
  public ExperienceExtractedOutput {
    entries = entries == null ? List.of() : List.copyOf(entries);
  }

  public static record ExperienceEntry(
      String headerLine,
      String company,
      String role,
      Integer startYear,
      Integer endYear,
      boolean isCurrent,
      List<String> details) {
    public ExperienceEntry {
      details = details == null ? List.of() : List.copyOf(details);
    }
  }

  public static final class ExperienceEntryBuilder {
    public String headerLine;
    public String company;
    public String role;
    public Integer startYear;
    public Integer endYear;
    public boolean isCurrent;
    public List<String> details = new ArrayList<>();

    public boolean hasMeaningfulContent() {
      return (headerLine != null && !headerLine.isBlank())
          || (company != null && !company.isBlank())
          || (role != null && !role.isBlank())
          || (startYear != null)
          || !details.isEmpty();
    }

    public ExperienceEntry build() {
      return new ExperienceEntry(
          headerLine, company, role, startYear, endYear, isCurrent, List.copyOf(details));
    }
  }
}
