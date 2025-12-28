package com.jobcopilot.profile_service.parser.dictionary;

import java.util.List;
import lombok.Getter;

@Getter
public enum Skill {
  JAVA(List.of("java"), false),
  SPRING(List.of("spring"), false),
  SPRING_BOOT(List.of("spring boot", "spring-boot"), false),
  KAFKA(List.of("kafka"), false),
  SQL(List.of("sql"), false),
  POSTGRESQL(List.of("postgresql", "postgres"), false),
  MONGODB(List.of("mongodb", "mongo"), false),
  DOCKER(List.of("docker"), false),
  KUBERNETES(List.of("kubernetes", "k8s"), false),
  AWS(List.of("aws", "amazon web services"), false),
  REACT(List.of("react", "react.js", "reactjs"), false),
  NEXTJS(List.of("next.js", "nextjs"), false),
  PYTHON(List.of("python"), false),
  GOLANG(List.of("golang", "go"), true),
  NODEJS(List.of("node.js", "nodejs", "node"), false);

  private final List<String> aliases;
  private boolean ambiguous;

  Skill(List<String> aliases, boolean ambiguous) {
    this.aliases = aliases;
    this.ambiguous = ambiguous;
  }
}
