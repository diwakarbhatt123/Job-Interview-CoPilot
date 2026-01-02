package com.jobcopilot.profile_service.config;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExecutorConfig {

  @Bean("profileTaskExecutor")
  public ExecutorService profileTaskExecutor() {
    return Executors.newFixedThreadPool(5);
  }

  @Bean("parserExecutor")
  public ExecutorService parserExecutor() {
    return Executors.newFixedThreadPool(5);
  }
}
