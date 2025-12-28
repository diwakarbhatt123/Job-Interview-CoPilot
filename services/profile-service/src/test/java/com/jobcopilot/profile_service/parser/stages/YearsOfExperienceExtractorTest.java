package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import com.jobcopilot.profile_service.parser.model.output.YearsExtractedOutput;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class YearsOfExperienceExtractorTest {

  private final YearsOfExperienceExtractor extractor = new YearsOfExperienceExtractor();

  @Test
  void prefersSummaryYearsWhenPresent() {
    SectionizedOutput.SectionDetail summary =
        SectionizedOutput.SectionDetail.builder()
            .name("SUMMARY")
            .section(ResumeSection.SUMMARY)
            .startLine(0)
            .endLine(1)
            .lines(List.of("10+ years of experience in backend systems"))
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(summary));

    YearsExtractedOutput years = (YearsExtractedOutput) extractor.process(output);

    assertThat(years.yearsOfExperience()).isEqualTo(10);
  }

  @Test
  void infersYearsFromExperienceDates() {
    int currentYear = LocalDate.now().getYear();
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(2)
            .lines(List.of("Jan 2018 - Present", "Acme Corp", "2012 - 2016"))
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(exp));

    YearsExtractedOutput years = (YearsExtractedOutput) extractor.process(output);

    assertThat(years.yearsOfExperience()).isEqualTo(currentYear - 2012);
  }

  @Test
  void handlesMultiLineMonthYearRange() {
    int currentYear = LocalDate.now().getYear();
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(3)
            .lines(List.of("Jan 2016 - Present", "Acme Corp"))
            .build();

    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(exp));

    YearsExtractedOutput years = (YearsExtractedOutput) extractor.process(output);

    assertThat(years.yearsOfExperience()).isEqualTo(currentYear - 2016);
  }
}
