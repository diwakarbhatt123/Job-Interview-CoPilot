package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.dictionary.Domain;
import com.jobcopilot.job_analyzer_service.parser.dictionary.Seniority;
import com.jobcopilot.job_analyzer_service.parser.model.output.ExtractedMetadataOutput;
import com.jobcopilot.job_analyzer_service.parser.model.request.JdAnalysisPipelineRequest;
import com.jobcopilot.parser.ParsingPipeline;
import com.jobcopilot.parser.PipelineBuilder;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ExtractedMetadataMergerTest {

  @Test
  void extractsFromBackendFixture() throws Exception {
    String text = readFixture("jd_backend_senior.txt");
    ExtractedMetadataOutput metadata = runPipeline(text);

    assertThat(metadata.seniority()).isEqualTo(Seniority.SENIOR);
    assertThat(metadata.domain()).isEqualTo(Domain.BACKEND);
    assertThat(metadata.requiredSkills()).contains("JAVA", "SPRING_BOOT", "SQL");
    assertThat(metadata.preferredSkills()).contains("TERRAFORM", "KUBERNETES");
  }

  @Test
  void extractsFromFullstackFixture() throws Exception {
    String text = readFixture("jd_fullstack_mid.txt");
    ExtractedMetadataOutput metadata = runPipeline(text);

    assertThat(metadata.seniority()).isEqualTo(Seniority.UNKNOWN);
    assertThat(metadata.domain()).isEqualTo(Domain.FULLSTACK);
    assertThat(metadata.requiredSkills()).contains("GO", "REST", "SQL");
    assertThat(metadata.preferredSkills()).contains("GRAPHQL", "DOCKER");
  }

  private String readFixture(String name) throws IOException {
    Path path = Path.of("src", "test", "resources", "fixtures", name);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  private ExtractedMetadataOutput runPipeline(String text) throws Exception {
    ParsingPipeline pipeline =
        PipelineBuilder.init()
            .addStage(new JdTextNormalizer())
            .addStage(new BlockLabeler())
            .addStage(new SeniorityExtractor())
            .addStage(new DomainExtractor())
            .addStage(new SkillExtractor())
            .addStage(new ExtractedMetadataMerger())
            .build();
    return (ExtractedMetadataOutput) pipeline.execute(new JdAnalysisPipelineRequest(text));
  }
}
