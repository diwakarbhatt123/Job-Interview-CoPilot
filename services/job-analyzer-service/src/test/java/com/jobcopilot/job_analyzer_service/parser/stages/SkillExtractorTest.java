package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.dictionary.BlockLabel;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import com.jobcopilot.job_analyzer_service.parser.model.SkillExtractionResult;
import java.util.List;
import org.junit.jupiter.api.Test;

class SkillExtractorTest {

  @Test
  void matchesAliasesAndClassifiesRequiredPreferred() {
    List<LabeledLine> lines =
        List.of(
            new LabeledLine(0, "Requirements", BlockLabel.OTHER),
            new LabeledLine(1, "Must have springboot and Java", BlockLabel.REQUIREMENTS),
            new LabeledLine(2, "Nice to have Kubernetes", BlockLabel.PREFERRED));

    SkillExtractor extractor = new SkillExtractor();
    SkillExtractionResult result = extractor.extract(lines);

    assertThat(result.requiredSkills()).contains("SPRING_BOOT", "JAVA");
    assertThat(result.preferredSkills()).contains("KUBERNETES");
  }

  @Test
  void avoidsSubstringFalsePositives() {
    List<LabeledLine> lines = List.of(new LabeledLine(0, "Django experience", BlockLabel.OTHER));

    SkillExtractor extractor = new SkillExtractor();
    SkillExtractionResult result = extractor.extract(lines);

    assertThat(result.techStack()).doesNotContain("GO");
  }
}
