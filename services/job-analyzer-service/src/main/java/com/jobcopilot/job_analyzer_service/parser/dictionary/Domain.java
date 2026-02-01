package com.jobcopilot.job_analyzer_service.parser.dictionary;

import java.util.List;

public enum Domain {
  BACKEND(List.of("backend", "microservices", "api", "server")),
  FRONTEND(List.of("frontend", "react", "ui", "ux", "browser")),
  FULLSTACK(List.of("fullstack", "full stack")),
  DATA(List.of("data pipeline", "etl", "warehouse", "analytics")),
  ML(List.of("machine learning", "ml", "model", "training")),
  PLATFORM(List.of("platform", "infrastructure", "runtime")),
  DEVOPS(List.of("devops", "ci/cd", "kubernetes", "terraform")),
  MOBILE(List.of("android", "ios", "mobile")),
  SECURITY(List.of("security", "threat", "vulnerability", "infosec")),
  UNKNOWN(List.of());

  private final List<String> keywords;

  Domain(List<String> keywords) {
    this.keywords = List.copyOf(keywords);
  }

  public List<String> keywords() {
    return List.copyOf(keywords);
  }
}
