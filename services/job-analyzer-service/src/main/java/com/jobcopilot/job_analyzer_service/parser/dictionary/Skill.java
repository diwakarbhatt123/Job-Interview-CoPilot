package com.jobcopilot.job_analyzer_service.parser.dictionary;

import java.util.List;

public enum Skill {
  JAVA(List.of("java")),
  SPRING(List.of("spring")),
  SPRING_BOOT(List.of("spring boot", "springboot")),
  SQL(List.of("sql")),
  POSTGRESQL(List.of("postgres", "postgresql", "postgre")),
  MYSQL(List.of("mysql")),
  MONGODB(List.of("mongo", "mongodb")),
  REDIS(List.of("redis")),
  KAFKA(List.of("kafka")),
  AWS(List.of("aws", "amazon web services")),
  DOCKER(List.of("docker")),
  KUBERNETES(List.of("kubernetes", "k8s")),
  TERRAFORM(List.of("terraform")),
  GIT(List.of("git")),
  CI_CD(List.of("ci/cd", "cicd", "continuous integration")),
  REST(List.of("rest", "restful")),
  GRAPHQL(List.of("graphql")),
  GO(List.of("go", "golang")),
  PYTHON(List.of("python"));

  private final List<String> aliases;

  Skill(List<String> aliases) {
    this.aliases = List.copyOf(aliases);
  }

  public List<String> aliases() {
    return List.copyOf(aliases);
  }
}
