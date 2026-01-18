package com.jobcopilot.job_analyzer_service;

import com.jobcopilot.job_analyzer_service.config.EmbeddedMongoTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@Import(EmbeddedMongoTestConfig.class)
@TestPropertySource(
    properties = {
      "spring.data.mongodb.database=job_analyzer_service_test",
      "spring.data.mongodb.auto-index-creation=true"
    })
class JobAnalyzerServiceApplicationTests {

  @Test
  void contextLoads() {}
}
