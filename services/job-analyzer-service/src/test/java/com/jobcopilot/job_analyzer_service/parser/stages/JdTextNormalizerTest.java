package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.model.output.NormalizedJdTextOutput;
import com.jobcopilot.job_analyzer_service.parser.model.request.JdAnalysisPipelineRequest;
import org.junit.jupiter.api.Test;

class JdTextNormalizerTest {

  @Test
  void normalizesWhitespaceBulletsAndNewlines() {
    String raw = "Line 1\r\n\t• Item\rLine 3  with   spaces";
    JdTextNormalizer normalizer = new JdTextNormalizer();

    NormalizedJdTextOutput result =
        (NormalizedJdTextOutput) normalizer.process(new JdAnalysisPipelineRequest(raw));

    assertThat(result.normalizedText()).isEqualTo("Line 1\n- Item\nLine 3 with spaces");
  }

  @Test
  void normalizesUnicodeNfkc() {
    String raw = "Ｆｕｌｌｗｉｄｔｈ";
    JdTextNormalizer normalizer = new JdTextNormalizer();

    NormalizedJdTextOutput result =
        (NormalizedJdTextOutput) normalizer.process(new JdAnalysisPipelineRequest(raw));

    assertThat(result.normalizedText()).isEqualTo("Fullwidth");
  }
}
