package com.jobcopilot.job_analyzer_service.config;

import com.jobcopilot.job_analyzer_service.parser.stages.*;
import com.jobcopilot.parser.ParsingPipeline;
import com.jobcopilot.parser.PipelineBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ParserConfig {

  @Bean
  public ParsingPipeline jobAnalysisPipeline() {
    return PipelineBuilder.init()
        .addStage(new JdTextNormalizer())
        .addStage(new BlockLabeler())
        .addStage(new SeniorityExtractor())
        .addStage(new DomainExtractor())
        .addStage(new SkillExtractor())
        .addStage(new ExtractedMetadataMerger())
        .build();
  }
}
