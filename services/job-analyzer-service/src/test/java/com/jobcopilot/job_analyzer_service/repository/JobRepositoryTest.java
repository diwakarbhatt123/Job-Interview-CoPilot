package com.jobcopilot.job_analyzer_service.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.config.EmbeddedMongoTestConfig;
import com.jobcopilot.job_analyzer_service.entity.Job;
import com.jobcopilot.job_analyzer_service.entity.values.Analysis;
import com.jobcopilot.job_analyzer_service.entity.values.Input;
import com.jobcopilot.job_analyzer_service.enums.AnalysisStatus;
import com.jobcopilot.job_analyzer_service.enums.InputType;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
class JobRepositoryTest {

  @Autowired private JobRepository jobRepository;

  @Test
  void findsByUserIdAndProfileId() {
    Job job =
        Job.builder()
            .userId("user-1")
            .profileId("profile-1")
            .analysis(new Analysis(AnalysisStatus.PENDING, 0, null, null, null, null, null, null))
            .input(
                new Input(InputType.PASTED, null, "raw", null, "normalized", "en", Instant.now()))
            .build();

    Job saved = jobRepository.save(job);

    var found = jobRepository.findByUserIdAndProfileId("user-1", "profile-1");
    assertThat(found).isPresent();
    assertThat(found.get().getId()).isEqualTo(saved.getId());
  }

  @Test
  void findsByAnalysisStatus() {
    Job pending =
        Job.builder()
            .userId("user-2")
            .profileId("profile-2")
            .analysis(new Analysis(AnalysisStatus.PENDING, 0, null, null, null, null, null, null))
            .input(
                new Input(InputType.PASTED, null, "raw", null, "normalized", "en", Instant.now()))
            .build();
    Job completed =
        Job.builder()
            .userId("user-3")
            .profileId("profile-3")
            .analysis(
                new Analysis(
                    AnalysisStatus.COMPLETED, 1, null, null, null, Instant.now(), null, null))
            .input(
                new Input(InputType.PASTED, null, "raw", null, "normalized", "en", Instant.now()))
            .build();

    jobRepository.save(pending);
    jobRepository.save(completed);

    List<Job> results = jobRepository.findByAnalysis_Status(AnalysisStatus.PENDING);
    assertThat(results).extracting(Job::getProfileId).contains("profile-2");
    assertThat(results).extracting(Job::getProfileId).doesNotContain("profile-3");
  }
}
