package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.dictionary.Skill;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import com.jobcopilot.profile_service.parser.model.output.SkillExtractedOutput;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillExtractorTest {

  private final SkillExtractor extractor = new SkillExtractor();

  @Test
  void extractsSkillsFromSkillsSectionIncludingAmbiguous() {
    SectionizedOutput.SectionDetail skills =
        SectionizedOutput.SectionDetail.builder()
            .name("SKILLS")
            .section(ResumeSection.SKILLS)
            .startLine(0)
            .endLine(1)
            .lines(List.of("Java, Spring Boot, Postgres, Go, AWS"))
            .build();
    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(skills));

    SkillExtractedOutput parsed = (SkillExtractedOutput) extractor.process(output);

    assertThat(parsed.skills())
        .contains(Skill.JAVA, Skill.SPRING_BOOT, Skill.POSTGRESQL, Skill.GOLANG, Skill.AWS);
  }

  @Test
  void skipsAmbiguousSkillsOutsideSkillsSection() {
    SectionizedOutput.SectionDetail exp =
        SectionizedOutput.SectionDetail.builder()
            .name("EXPERIENCE")
            .section(ResumeSection.EXPERIENCE)
            .startLine(0)
            .endLine(1)
            .lines(List.of("Worked with Go and Java"))
            .build();
    SectionizedOutput output =
        new SectionizedOutput("raw", "Worked with Go and Java", List.of(exp));

    SkillExtractedOutput parsed = (SkillExtractedOutput) extractor.process(output);

    assertThat(parsed.skills()).contains(Skill.JAVA);
    assertThat(parsed.skills()).doesNotContain(Skill.GOLANG);
  }

  @Test
  void handlesSpecialCharacterSkills() {
    SectionizedOutput.SectionDetail skills =
        SectionizedOutput.SectionDetail.builder()
            .name("SKILLS")
            .section(ResumeSection.SKILLS)
            .startLine(0)
            .endLine(1)
            .lines(List.of("Node.js, React.js, Next.js"))
            .build();
    SectionizedOutput output = new SectionizedOutput("raw", "norm", List.of(skills));

    SkillExtractedOutput parsed = (SkillExtractedOutput) extractor.process(output);

    assertThat(parsed.skills()).contains(Skill.NODEJS, Skill.REACT, Skill.NEXTJS);
  }
}
