package com.jobcopilot.fit_score_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FitScoreServiceApplication {

  private FitScoreServiceApplication() {
    throw new IllegalStateException("Utility class");
  }

  public static void main(String[] args) {
    SpringApplication.run(FitScoreServiceApplication.class, args);
  }
}
