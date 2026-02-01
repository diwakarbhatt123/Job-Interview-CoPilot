package com.jobcopilot.job_analyzer_service.parser.dictionary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public enum Seniority {
  PRINCIPAL(List.of("principal"), 1),
  STAFF(List.of("staff"), 2),
  LEAD(List.of("lead", "leading"), 3),
  MANAGER(List.of("manager", "management"), 4),
  SENIOR(List.of("senior", "sr"), 5),
  MID(List.of("mid", "mid-level"), 6),
  JUNIOR(List.of("junior", "jr", "entry"), 7),
  INTERN(List.of("intern", "internship"), 8),
  UNKNOWN(List.of(), 9);

  private final List<String> aliases;
  private final int precedence;

  Seniority(List<String> aliases, int precedence) {
    this.aliases = List.copyOf(aliases);
    this.precedence = precedence;
  }

  public static List<Seniority> orderedByPrecedence() {
    List<Seniority> ordered = new ArrayList<>(List.of(values()));
    ordered.sort(Comparator.comparingInt(Seniority::precedence));
    return List.copyOf(ordered);
  }

  public List<String> aliases() {
    return List.copyOf(aliases);
  }

  public int precedence() {
    return precedence;
  }
}
