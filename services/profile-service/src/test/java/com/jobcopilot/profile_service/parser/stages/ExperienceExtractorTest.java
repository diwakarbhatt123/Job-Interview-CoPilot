package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.output.ExperienceExtractedOutput;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExperienceExtractorTest {

  private final ExperienceExtractor extractor = new ExperienceExtractor();

  @Test
  void extractsCompanyRoleDatesAndDetails() {
    List<String> lines =
        List.of("Acme Corp — Senior Engineer — 2019 - Present", "- Built APIs", "- Led team");
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(2)
            .lines(lines)
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(exp));

    ExperienceExtractedOutput parsed = (ExperienceExtractedOutput) extractor.process(output);

    assertThat(parsed.entries()).hasSize(1);
    ExperienceExtractedOutput.ExperienceEntry entry = parsed.entries().getFirst();
    assertThat(entry.company()).isEqualTo("Acme Corp");
    assertThat(entry.role()).isEqualTo("Senior Engineer");
    assertThat(entry.startYear()).isEqualTo(2019);
    assertThat(entry.isCurrent()).isTrue();
    assertThat(entry.details()).containsExactly("Built APIs", "Led team");
  }

  @Test
  void doesNotTreatDatesAsCompanyOrRole() {
    List<String> lines = List.of("Beta LLC - 2016 - 2018", "- Migrated services");
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(1)
            .lines(lines)
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(exp));

    ExperienceExtractedOutput parsed = (ExperienceExtractedOutput) extractor.process(output);

    ExperienceExtractedOutput.ExperienceEntry entry = parsed.entries().getFirst();
    assertThat(entry.company()).isEqualTo("Beta LLC");
    assertThat(entry.role()).isNull();
    assertThat(entry.startYear()).isEqualTo(2016);
    assertThat(entry.endYear()).isEqualTo(2018);
  }

  @Test
  void handlesHeaderWithoutRoleKeyword() {
    List<String> lines = List.of("Gamma Co | 2014 - 2016", "- Built services");
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(1)
            .lines(lines)
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(exp));

    ExperienceExtractedOutput parsed = (ExperienceExtractedOutput) extractor.process(output);

    ExperienceExtractedOutput.ExperienceEntry entry = parsed.entries().getFirst();
    assertThat(entry.company()).isEqualTo("Gamma Co");
    assertThat(entry.role()).isNull();
    assertThat(entry.startYear()).isEqualTo(2014);
    assertThat(entry.endYear()).isEqualTo(2016);
  }
}
