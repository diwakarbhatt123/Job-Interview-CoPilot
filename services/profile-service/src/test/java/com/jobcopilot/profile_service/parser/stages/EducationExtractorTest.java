package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.output.EducationExtractedOutput;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import java.util.List;
import org.junit.jupiter.api.Test;

class EducationExtractorTest {

  private final EducationExtractor extractor = new EducationExtractor();

  @Test
  void extractsInstitutionDegreeFieldAndYears() {
    List<String> lines = List.of("State University", "B.Sc. in Computer Science", "2014 - 2018");
    SectionizedOutput.SectionDetail edu =
        SectionizedOutput.SectionDetail.builder()
            .name("EDUCATION")
            .section(ResumeSection.EDUCATION)
            .startLine(0)
            .endLine(2)
            .lines(lines)
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(edu));

    EducationExtractedOutput parsed = (EducationExtractedOutput) extractor.process(output);

    assertThat(parsed.entries()).hasSize(1);
    EducationExtractedOutput.EducationEntry entry = parsed.entries().getFirst();
    assertThat(entry.institution()).contains("State University");
    assertThat(entry.degree()).contains("B.Sc");
    assertThat(entry.field()).contains("Computer Science");
    assertThat(entry.startYear()).isEqualTo(2014);
    assertThat(entry.endYear()).isEqualTo(2018);
  }

  @Test
  void handlesSingleGraduationYear() {
    List<String> lines = List.of("Institute of Technology", "M.Sc. in Data Science", "2020");
    SectionizedOutput.SectionDetail edu =
        SectionizedOutput.SectionDetail.builder()
            .name("EDUCATION")
            .section(ResumeSection.EDUCATION)
            .startLine(0)
            .endLine(2)
            .lines(lines)
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(edu));

    EducationExtractedOutput parsed = (EducationExtractedOutput) extractor.process(output);

    EducationExtractedOutput.EducationEntry entry = parsed.entries().getFirst();
    assertThat(entry.endYear()).isEqualTo(2020);
    assertThat(entry.startYear()).isNull();
  }
}
