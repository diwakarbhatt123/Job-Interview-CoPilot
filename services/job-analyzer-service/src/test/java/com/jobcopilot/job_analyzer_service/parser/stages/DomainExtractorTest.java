package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.model.DomainResult;
import org.junit.jupiter.api.Test;

class DomainExtractorTest {

  @Test
  void returnsUnknownWhenSignalsWeak() {
    DomainExtractor extractor = new DomainExtractor();
    DomainResult result = extractor.extract("Looking for an engineer");
    assertThat(result.domain()).isEqualTo(Domain.UNKNOWN);
  }

  @Test
  void detectsBackendOnStrongSignals() {
    DomainExtractor extractor = new DomainExtractor();
    DomainResult result = extractor.extract("Backend microservices API server");
    assertThat(result.domain()).isEqualTo(Domain.BACKEND);
  }
}
