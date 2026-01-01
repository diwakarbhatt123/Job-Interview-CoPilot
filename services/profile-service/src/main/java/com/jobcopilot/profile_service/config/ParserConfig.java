package com.jobcopilot.profile_service.config;

import com.jobcopilot.profile_service.parser.ParsingPipeline;
import com.jobcopilot.profile_service.parser.PipelineBuilder;
import com.jobcopilot.profile_service.parser.stages.*;
import java.util.concurrent.ExecutorService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserConfig {

  @Bean
  public ParsingPipeline resumeParsingPipeline(ExecutorService executorService) {
    return PipelineBuilder.init(executorService)
        .addStage(new TextNormalizer())
        .addStage(new Sectionizer())
        .addStage(new YearsOfExperienceExtractor())
        .addStage(new ExperienceExtractor())
        .addStage(new EducationExtractor())
        .addStage(new SkillExtractor())
        .addStage(new ExtractedResumeDataMerger())
        .build();
  }

  @Bean
  public ParsingPipeline resumePdfParsingPipeline(ExecutorService executorService) {
    return PipelineBuilder.init(executorService)
        .addStage(new PdfToTextExtractor())
        .addStage(new TextNormalizer())
        .addStage(new Sectionizer())
        .addStage(new YearsOfExperienceExtractor())
        .addStage(new ExperienceExtractor())
        .addStage(new EducationExtractor())
        .addStage(new SkillExtractor())
        .addStage(new ExtractedResumeDataMerger())
        .build();
  }
}
