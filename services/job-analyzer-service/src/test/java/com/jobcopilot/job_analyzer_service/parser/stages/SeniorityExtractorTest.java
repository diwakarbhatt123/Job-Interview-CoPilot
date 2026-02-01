package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.SeniorityResult;
import org.junit.jupiter.api.Test;

class SeniorityExtractorTest {

  @Test
  void prefersTitleMatchesWithPrecedence() {
    String text = "Senior Lead Engineer\nResponsibilities\n";
    SeniorityExtractor extractor = new SeniorityExtractor();

    SeniorityResult result = extractor.extract(text);

    assertThat(result.seniority()).isEqualTo(Seniority.LEAD);
  }

  @Test
  void fallsBackToBodyWhenNoTitleMatch() {
    String text = "Engineering Role\nWe are seeking a staff engineer";
    SeniorityExtractor extractor = new SeniorityExtractor();

    SeniorityResult result = extractor.extract(text);

    assertThat(result.seniority()).isEqualTo(Seniority.STAFF);
  }
}
