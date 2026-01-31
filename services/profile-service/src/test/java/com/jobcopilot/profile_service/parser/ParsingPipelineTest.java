package com.jobcopilot.profile_service.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.parser.ParsingPipeline;
import com.jobcopilot.parser.PipelineBuilder;
import com.jobcopilot.parser.model.request.PlainTextAnalysisPipelineRequest;
import com.jobcopilot.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.parser.stages.EducationExtractor;
import com.jobcopilot.profile_service.parser.stages.ExperienceExtractor;
import com.jobcopilot.profile_service.parser.stages.ExtractedResumeDataMerger;
import com.jobcopilot.profile_service.parser.stages.Sectionizer;
import com.jobcopilot.profile_service.parser.stages.SkillExtractor;
import com.jobcopilot.profile_service.parser.stages.TextNormalizer;
import com.jobcopilot.profile_service.parser.stages.YearsOfExperienceExtractor;
import org.junit.jupiter.api.Test;

class ParsingPipelineTest {

  @Test
  void runsEndToEndPipelineAndProducesResponse() throws Exception {
    String input =
        """
            SUMMARY
            5+ years of experience
            EXPERIENCE
            Acme Corp — Engineer — 2020 - Present
            - Built APIs
            EDUCATION
            State University
            B.Sc. in Computer Science
            2014 - 2018
            SKILLS
            Java, Spring Boot, AWS
            """;

    ParsingPipeline pipeline =
        PipelineBuilder.init()
            .addStage(new TextNormalizer())
            .addStage(new Sectionizer())
            .addStage(new YearsOfExperienceExtractor())
            .addStage(new ExperienceExtractor())
            .addStage(new EducationExtractor())
            .addStage(new SkillExtractor())
            .addStage(new ExtractedResumeDataMerger())
            .build();

    PipelineResponse response = pipeline.execute(new PlainTextAnalysisPipelineRequest(input));

    assertThat(response.rawText()).isEqualTo(input);
    assertThat(response.normalizedText()).contains("SUMMARY");
    assertThat(response.yearsOfExperience()).isEqualTo(5);
    assertThat(response.experiences()).hasSize(1);
    assertThat(response.experiences().getFirst().company()).isEqualTo("Acme Corp");
    assertThat(response.experiences().getFirst().role()).isEqualTo("Engineer");
    assertThat(response.experiences().getFirst().startYear()).isEqualTo(2020);
    assertThat(response.experiences().getFirst().isCurrent()).isTrue();
    assertThat(response.educations()).hasSize(1);
    assertThat(response.educations().getFirst().institution()).contains("State University");
    assertThat(response.educations().getFirst().degree()).contains("B.Sc");
    assertThat(response.educations().getFirst().field()).contains("Computer Science");
    assertThat(response.educations().getFirst().startYear()).isEqualTo(2014);
    assertThat(response.educations().getFirst().endYear()).isEqualTo(2018);
    assertThat(response.skills()).isNotEmpty();

    pipeline.close();
  }
}
