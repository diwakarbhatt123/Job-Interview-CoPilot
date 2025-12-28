package com.jobcopilot.profile_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.model.output.NormalizedTextOutput;
import com.jobcopilot.profile_service.parser.model.request.PlainTextAnalysisPipelineRequest;
import org.junit.jupiter.api.Test;

class TextNormalizerTest {

  private final TextNormalizer normalizer = new TextNormalizer();

  @Test
  void normalizesWhitespaceBulletsAndHeaders() {
    String input =
        "SUMMARY:\r\n"
            + "•  Built\t systems  \r\n"
            + "—  Led team\r\n"
            + "\r\n"
            + "EXPERIENCE:\r\n"
            + "Software Engineer\r\n";

    NormalizedTextOutput output =
        (NormalizedTextOutput) normalizer.process(new PlainTextAnalysisPipelineRequest(input));

    assertThat(output.rawText()).isEqualTo(input);
    assertThat(output.normalizedText())
        .contains("SUMMARY")
        .doesNotContain("SUMMARY:")
        .contains("- Built systems")
        .contains("- Led team")
        .contains("EXPERIENCE");
  }

  @Test
  void repairsLineWrappingForLowercaseContinuationOnly() {
    String input =
        "Professional Summary\n"
            + "Experienced\n"
            + "engineer with backend focus\n"
            + "EXPERIENCE\n"
            + "Acme Corp\n";

    NormalizedTextOutput output =
        (NormalizedTextOutput) normalizer.process(new PlainTextAnalysisPipelineRequest(input));

    // "Experienced" + "engineer..." should merge; header boundary should not merge.
    assertThat(output.normalizedText()).contains("Experienced engineer with backend focus");
    assertThat(output.normalizedText()).contains("EXPERIENCE\nAcme Corp");
  }
}
