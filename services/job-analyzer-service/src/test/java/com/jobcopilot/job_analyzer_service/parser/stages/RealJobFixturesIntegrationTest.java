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
import java.util.List;
import org.junit.jupiter.api.Test;

class RealJobFixturesIntegrationTest {

  @Test
  void extractsMetadataFromRealJobFixtures() throws Exception {
    for (Fixture fixture : fixtures()) {
      String text = readFixture(fixture.fileName());
      ExtractedMetadataOutput metadata = runPipeline(text);

      assertThat(metadata.seniority())
          .as("fixture=%s", fixture.fileName())
          .isEqualTo(fixture.seniority());
      assertThat(metadata.domain())
          .as("fixture=%s", fixture.fileName())
          .isEqualTo(fixture.domain());
      assertThat(metadata.requiredSkills())
          .as("fixture=%s", fixture.fileName())
          .containsExactlyElementsOf(fixture.requiredSkills());
      assertThat(metadata.preferredSkills())
          .as("fixture=%s", fixture.fileName())
          .containsExactlyElementsOf(fixture.preferredSkills());
      assertThat(metadata.techStack())
          .as("fixture=%s", fixture.fileName())
          .containsExactlyElementsOf(fixture.techStack());
    }
  }

  private List<Fixture> fixtures() {
    return List.of(
        new Fixture(
            "real_jd_appzen_backend.txt",
            Seniority.SENIOR,
            Domain.BACKEND,
            List.of(),
            List.of("CI_CD", "DOCKER", "GIT", "KUBERNETES"),
            List.of(
                "CI_CD",
                "DOCKER",
                "GIT",
                "GO",
                "JAVA",
                "KUBERNETES",
                "MYSQL",
                "POSTGRESQL",
                "REST",
                "SPRING",
                "SQL")),
        new Fixture(
            "real_jd_hive_frontend.txt",
            Seniority.UNKNOWN,
            Domain.FRONTEND,
            List.of(),
            List.of(),
            List.of()),
        new Fixture(
            "real_jd_3pillar_fullstack.txt",
            Seniority.LEAD,
            Domain.FULLSTACK,
            List.of(
                "AWS",
                "CI_CD",
                "DOCKER",
                "JAVA",
                "KUBERNETES",
                "MONGODB",
                "MYSQL",
                "POSTGRESQL",
                "REST",
                "SPRING_BOOT",
                "SQL"),
            List.of(),
            List.of(
                "AWS",
                "CI_CD",
                "DOCKER",
                "JAVA",
                "KUBERNETES",
                "MONGODB",
                "MYSQL",
                "POSTGRESQL",
                "REST",
                "SPRING_BOOT",
                "SQL")),
        new Fixture(
            "real_jd_decentraland_data.txt",
            Seniority.SENIOR,
            Domain.DATA,
            List.of("AWS", "PYTHON"),
            List.of(),
            List.of("AWS", "PYTHON")),
        new Fixture(
            "real_jd_squire_ml.txt",
            Seniority.UNKNOWN,
            Domain.ML,
            List.of("AWS", "CI_CD", "PYTHON"),
            List.of(),
            List.of("AWS", "CI_CD", "PYTHON")),
        new Fixture(
            "real_jd_cority_platform.txt",
            Seniority.SENIOR,
            Domain.PLATFORM,
            List.of("AWS", "CI_CD", "TERRAFORM"),
            List.of("PYTHON"),
            List.of("AWS", "CI_CD", "PYTHON", "TERRAFORM")),
        new Fixture(
            "real_jd_agiloft_devops.txt",
            Seniority.SENIOR,
            Domain.DEVOPS,
            List.of("AWS", "PYTHON", "TERRAFORM"),
            List.of(),
            List.of("AWS", "CI_CD", "DOCKER", "KUBERNETES", "PYTHON", "TERRAFORM")),
        new Fixture(
            "real_jd_ahead_security.txt",
            Seniority.MANAGER,
            Domain.SECURITY,
            List.of(),
            List.of(),
            List.of("AWS")),
        new Fixture(
            "real_jd_whoop_ios.txt",
            Seniority.SENIOR,
            Domain.MOBILE,
            List.of("REST"),
            List.of(),
            List.of("REST")),
        new Fixture(
            "real_jd_coins_android.txt",
            Seniority.UNKNOWN,
            Domain.MOBILE,
            List.of("JAVA"),
            List.of(),
            List.of("JAVA")));
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

  private record Fixture(
      String fileName,
      Seniority seniority,
      Domain domain,
      List<String> requiredSkills,
      List<String> preferredSkills,
      List<String> techStack) {}
}
