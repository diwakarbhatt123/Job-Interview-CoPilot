package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.parser.dictionary.ResumeSection;
import com.jobcopilot.parser.model.output.NormalizedTextOutput;
import com.jobcopilot.parser.model.output.SectionizedOutput;
import org.junit.jupiter.api.Test;

class SectionizerTest {

  private final Sectionizer sectionizer = new Sectionizer();

  @Test
  void detectsStandardSectionsAndAliases() {
    String text =
        """
            SUMMARY
            Backend engineer
            WORK EXPERIENCE
            Acme Corp
            EDUCATION:
            State University
            SKILLS
            Java, Spring
            """;

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
        """
            This line is not a header
            Experience in backend systems
            EXPERIENCE
            Acme Corp
            """;

    SectionizedOutput output =
        (SectionizedOutput) sectionizer.process(new NormalizedTextOutput(text, text));

    assertThat(output.sections()).hasSize(1);
    assertThat(output.sections().getFirst().getSection()).isEqualTo(ResumeSection.EXPERIENCE);
  }

  @Test
  void handlesAllCapsHeadersWithPunctuation() {
    String text =
        """
            SKILLS:
            Java, Spring
            PROJECTS -
            Parser Pipeline
            EDUCATION:
            State University
            """;

    SectionizedOutput output =
        (SectionizedOutput) sectionizer.process(new NormalizedTextOutput(text, text));

    assertThat(output.sections()).hasSize(3);
    assertThat(output.sections().get(0).getSection()).isEqualTo(ResumeSection.SKILLS);
    assertThat(output.sections().get(1).getSection()).isEqualTo(ResumeSection.PROJECTS);
    assertThat(output.sections().get(2).getSection()).isEqualTo(ResumeSection.EDUCATION);
  }
}
