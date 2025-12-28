package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.output.NormalizedTextOutput;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import org.junit.jupiter.api.Test;

class SectionizerTest {

  private final Sectionizer sectionizer = new Sectionizer();

  @Test
  void detectsStandardSectionsAndAliases() {
    String text =
        "SUMMARY\n"
            + "Backend engineer\n"
            + "WORK EXPERIENCE\n"
            + "Acme Corp\n"
            + "EDUCATION:\n"
            + "State University\n"
            + "SKILLS\n"
            + "Java, Spring\n";

    SectionizedOutput output =
        (SectionizedOutput) sectionizer.process(new NormalizedTextOutput(text, text));

    assertThat(output.sections()).hasSize(4);
    assertThat(output.sections().get(0).getSection()).isEqualTo(ResumeSection.SUMMARY);
    assertThat(output.sections().get(1).getSection()).isEqualTo(ResumeSection.EXPERIENCE);
    assertThat(output.sections().get(2).getSection()).isEqualTo(ResumeSection.EDUCATION);
    assertThat(output.sections().get(3).getSection()).isEqualTo(ResumeSection.SKILLS);

    assertThat(output.sections().get(1).getLines()).contains("Acme Corp");
  }

  @Test
  void ignoresNonHeaderLines() {
    String text =
        "This line is not a header\n"
            + "Experience in backend systems\n"
            + "EXPERIENCE\n"
            + "Acme Corp\n";

    SectionizedOutput output =
        (SectionizedOutput) sectionizer.process(new NormalizedTextOutput(text, text));

    assertThat(output.sections()).hasSize(1);
    assertThat(output.sections().get(0).getSection()).isEqualTo(ResumeSection.EXPERIENCE);
  }

  @Test
  void handlesAllCapsHeadersWithPunctuation() {
    String text =
        "SKILLS:\n"
            + "Java, Spring\n"
            + "PROJECTS -\n"
            + "Parser Pipeline\n"
            + "EDUCATION:\n"
            + "State University\n";

    SectionizedOutput output =
        (SectionizedOutput) sectionizer.process(new NormalizedTextOutput(text, text));

    assertThat(output.sections()).hasSize(3);
    assertThat(output.sections().get(0).getSection()).isEqualTo(ResumeSection.SKILLS);
    assertThat(output.sections().get(1).getSection()).isEqualTo(ResumeSection.PROJECTS);
    assertThat(output.sections().get(2).getSection()).isEqualTo(ResumeSection.EDUCATION);
  }
}
