package com.jobcopilot.profile_service.parser.model.output;

import com.jobcopilot.profile_service.parser.dictionary.ResumeSection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public record SectionizedOutput(String rawText, String normalizedText, List<SectionDetail> sections)
    implements StageOutput {
  public SectionizedOutput {
    sections = sections == null ? List.of() : List.copyOf(sections);
  }

  @Builder(toBuilder = true)
  @Getter
  public static class SectionDetail {
    private final String name;
    private final ResumeSection section;
    private final int startLine;
    private final int endLine;
    private final List<String> lines;

    public SectionDetail(
        String name, ResumeSection section, int startLine, int endLine, List<String> lines) {
      this.name = name;
      this.section = section;
      this.startLine = startLine;
      this.endLine = endLine;
      this.lines = lines == null ? List.of() : List.copyOf(lines);
    }
  }
}
