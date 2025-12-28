package com.jobcopilot.profile_service.parser.model.output;

import java.util.ArrayList;
import java.util.List;

public record EducationExtractedOutput(List<EducationEntry> entries) implements StageOutput {
  public EducationExtractedOutput {
    entries = entries == null ? List.of() : List.copyOf(entries);
  }

  public static final class EducationEntryBuilder {
    public final List<String> lines = new ArrayList<>();

    public boolean hasMeaningfulContent() {
      return !lines.isEmpty();
    }

    public EducationEntry build() {
      return new EducationEntry(null, null, null, null, null, List.copyOf(lines));
    }
  }

  public static record EducationEntry(
      String institution,
      String degree,
      String field,
      Integer startYear,
      Integer endYear,
      List<String> lines) {
    public EducationEntry {
      lines = lines == null ? List.of() : List.copyOf(lines);
    }
  }
}
