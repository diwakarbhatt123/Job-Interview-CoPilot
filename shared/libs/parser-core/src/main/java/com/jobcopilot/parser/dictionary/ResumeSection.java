package com.jobcopilot.parser.dictionary;

public enum ResumeSection {
  EXPERIENCE,
  SKILLS,
  EDUCATION,
  PROJECTS,
  SUMMARY,
  AWARDS,
  UNKNOWN;

  public static ResumeSection fromAliasOrUnknown(String headerLower) {
    ResumeSection s = fromAlias(headerLower);
    return s == null ? UNKNOWN : s;
  }

  public static ResumeSection fromAlias(String headerLower) {
    if (headerLower == null) return null;
    return switch (headerLower) {
      case "experience",
          "work experience",
          "employment",
          "professional experience",
          "work history" ->
          EXPERIENCE;
      case "skills", "technical skills", "core skills", "key skills", "technologies" -> SKILLS;
      case "education", "academics", "qualifications" -> EDUCATION;
      case "projects", "personal projects" -> PROJECTS;
      case "summary", "profile", "about me", "professional summary" -> SUMMARY;
      case "award", "awards" -> AWARDS;
      default -> null;
    };
  }
}
