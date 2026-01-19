package com.jobcopilot.job_analyzer_service.config;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
public class ExecutorConfig {

  @Bean
  public ExecutorService getExecutor(
      @Value("${poller.workerThreads}") Integer workerThreads,
      @Value("${poller.queueSize}") Integer queueSize) {
    return new ThreadPoolExecutor(
        workerThreads,
        workerThreads,
        0L,
        TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<>(queueSize),
        new ThreadPoolExecutor.CallerRunsPolicy());
  }
}
