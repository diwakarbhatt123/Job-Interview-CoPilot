package com.jobcopilot.profile_service.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.profile_service.parser.dictionary.Skill;
import com.jobcopilot.profile_service.parser.model.request.PlainTextAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.model.response.PipelineResponse;
import com.jobcopilot.profile_service.parser.stages.EducationExtractor;
import com.jobcopilot.profile_service.parser.stages.ExperienceExtractor;
import com.jobcopilot.profile_service.parser.stages.ExtractedResumeDataMerger;
import com.jobcopilot.profile_service.parser.stages.Sectionizer;
import com.jobcopilot.profile_service.parser.stages.SkillExtractor;
import com.jobcopilot.profile_service.parser.stages.TextNormalizer;
import com.jobcopilot.profile_service.parser.stages.YearsOfExperienceExtractor;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParsingPipelineTemplatesIT {

  private static Stream<Arguments> templates() {
    return Stream.of(
        //        Arguments.of(
        //            "Combination template with skills + work history",
        //            new TemplateCase(
        //                String.join(
        //                    "\n",
        //                    "Ava Jordan",
        //                    "SUMMARY",
        //                    "6 years of experience building backend services",
        //                    "SELECTED SKILLS",
        //                    "Java, Spring Boot, AWS, Docker",
        //                    "WORK HISTORY",
        //                    "Senior Engineer — Acme Corp — Jan 2018 - Present",
        //                    "- Built APIs",
        //                    "EDUCATION",
        //                    "State University",
        //                    "B.Sc. in Computer Science",
        //                    "2014 - 2018"),
        //                true,
        //                true,
        //                true,
        //                true,
        //                Set.of(Skill.JAVA, Skill.SPRING_BOOT, Skill.AWS, Skill.DOCKER),
        //                "State University",
        //                "Acme Corp",
        //                "Senior Engineer",
        //                6)),
        //        Arguments.of(
        //            "Chronological template with summary + skills",
        //            new TemplateCase(
        //                String.join(
        //                    "\n",
        //                    "SUMMARY",
        //                    "8 years of experience in platform engineering",
        //                    "WORK EXPERIENCE",
        //                    "Platform Engineer — Beta LLC — 2016 - 2020",
        //                    "- Built pipelines",
        //                    "Platform Engineer — Gamma Co — 2020 - Present",
        //                    "EDUCATION",
        //                    "Institute of Technology",
        //                    "B.Sc. in Software Engineering",
        //                    "2011 - 2015",
        //                    "SKILLS",
        //                    "Kubernetes, Kafka, PostgreSQL"),
        //                true,
        //                true,
        //                true,
        //                true,
        //                Set.of(Skill.KUBERNETES, Skill.KAFKA, Skill.POSTGRESQL),
        //                "Institute of Technology",
        //                "Beta LLC",
        //                "Platform Engineer",
        //                8)),
        //        Arguments.of(
        //            "Functional template with skills emphasis",
        //            new TemplateCase(
        //                String.join(
        //                    "\n",
        //                    "SUMMARY",
        //                    "5+ years of experience in data platforms",
        //                    "SKILLS",
        //                    "Python, MongoDB, SQL",
        //                    "EXPERIENCE",
        //                    "Data Engineer — Delta Labs — 2019 - Present",
        //                    "- Built ETL",
        //                    "EDUCATION",
        //                    "City College",
        //                    "M.Sc. in Data Science",
        //                    "2017 - 2019"),
        //                true,
        //                true,
        //                true,
        //                true,
        //                Set.of(Skill.PYTHON, Skill.MONGODB, Skill.SQL),
        //                "City College",
        //                "Delta Labs",
        //                "Data Engineer",
        //                5)),
        //        Arguments.of(
        //            "All caps headers with punctuation",
        //            new TemplateCase(
        //                String.join(
        //                    "\n",
        //                    "SUMMARY:",
        //                    "7 years of experience in distributed systems",
        //                    "SKILLS:",
        //                    "Go, AWS, Kubernetes",
        //                    "PROJECTS -",
        //                    "Parser Pipeline",
        //                    "WORK EXPERIENCE",
        //                    "Site Reliability Engineer — Zeta Inc — 2015 - 2022",
        //                    "EDUCATION:",
        //                    "Tech University",
        //                    "B.Sc. in Computer Science",
        //                    "2010 - 2014"),
        //                true,
        //                true,
        //                true,
        //                true,
        //                Set.of(Skill.AWS, Skill.KUBERNETES),
        //                "Tech University",
        //                "Zeta Inc",
        //                "Site Reliability Engineer",
        //                7)),
        //        Arguments.of(
        //            "Minimal text without headers",
        //            new TemplateCase(
        //                String.join(
        //                    "\n",
        //                    "Jamie Park",
        //                    "Backend engineer with platform focus",
        //                    "Built services and pipelines",
        //                    "Tech University 2012"),
        //                false,
        //                false,
        //                false,
        //                false,
        //                Set.of(),
        //                null,
        //                null,
        //                null,
        //                null)),
        Arguments.of(
            "Detailed senior engineer resume (Diwakar Bhatt)",
            new TemplateCase(
                String.join(
                    "\n",
                    "Diwakar Bhatt",
                    "+4915774433389 | diwakarbhatt68@gmail.com | linkedin.com/in/diwakar-bhatt | Berlin, Germany",
                    "Professional Summary",
                    "Senior Software Developer with 8+ years of experience in building scalable, high-performance distributed",
                    "systems. Specialized in Java, Golang, AWS, Kubernetes, and microservices architecture. Proven ability to",
                    "design and optimize large-scale applications, handling millions of requests daily. Skilled in cloud-native",
                    "development, system design, and performance tuning, ensuring low-latency, high-availability solutions.",
                    "Award-winning contributor, recognized for driving innovation, improving efficiency, and scaling critical",
                    "backend services.",
                    "Technical Skills",
                    "Languages: Java, Golang, Kotlin, Python, SQL",
                    "Frameworks and Libraries: Spring Boot, Dropwizard, JUnit",
                    "Developer Tools and Middleware: Git, Docker, Kubernetes, Nginx, Zookeeper, Jenkins",
                    "Architecture and Design: Microservices, Design Patterns, Distributed Systems, Cloud Native, Object Oriented",
                    "Design, Containerization",
                    "Databases: PostgreSQL, MySQL, MS SQL, MongoDB, Redis",
                    "Cloud Technologies: Amazon Web Services",
                    "Operating Systems: Linux, Windows",
                    "Messaging Systems: Kafka, RabbitMQ, SQS",
                    "Observability Tools: Kibana, Grafana",
                    "Experience",
                    "Senior Software Development Engineer Apr 2024 – Present",
                    "HelloFresh SE Berlin, Germany",
                    "• Enhanced & built features for a set of microservices facilitating seamless data flow across 20+ warehouses and the",
                    "HelloFresh ecosystem, improving system performance by optimizing core logic and improving locking mechanisms.",
                    "Software Development Engineer 2 Jan 2021 – Apr 2024",
                    "Flipkart Internet Private Limited Bengaluru, India",
                    "Lead Software Development Engineer Apr 2019 – Jan 2021",
                    "Freecharge Payment Technologies Pvt Ltd Gurugram, India",
                    "Software Development Engineer Aug 2018 - Apr 2019",
                    "Freecharge Payment Technologies Pvt Ltd Gurugram, India",
                    "Software Engineer Jan 2018 – Aug 2018",
                    "Bharti Airtel Gurugram, India",
                    "Engineer Technology Aug 2016 – Jan 2018",
                    "To The New Noida, India",
                    "Education",
                    "Maharshi Dayanand University Gurugram, India",
                    "Bachelor of Technology in Computer Science",
                    "Awards",
                    "Instant Karma Award 12/2022",
                    "Mission Impossible Award 12/2021"),
                true,
                true,
                true,
                true,
                Set.of(
                    Skill.JAVA,
                    Skill.GOLANG,
                    Skill.PYTHON,
                    Skill.SQL,
                    Skill.SPRING_BOOT,
                    Skill.DOCKER,
                    Skill.KUBERNETES,
                    Skill.AWS,
                    Skill.POSTGRESQL,
                    Skill.MONGODB,
                    Skill.KAFKA),
                "Maharshi Dayanand University",
                "HelloFresh SE",
                "Senior Software Development Engineer",
                8)));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("templates")
  void parsesMultipleResumeTemplates(String name, TemplateCase tc) throws Exception {
    ParsingPipeline pipeline =
        PipelineBuilder.init()
            .addStage(new TextNormalizer())
            .addStage(new Sectionizer())
            .addStage(new YearsOfExperienceExtractor())
            .addStage(new ExperienceExtractor())
            .addStage(new EducationExtractor())
            .addStage(new SkillExtractor())
            .addStage(new ExtractedResumeDataMerger())
            .build();

    PipelineResponse response = pipeline.execute(new PlainTextAnalysisPipelineRequest(tc.text));

    assertThat(response.rawText()).isEqualTo(tc.text);
    assertThat(response.normalizedText()).isNotBlank();

    if (tc.expectEducation) {
      assertThat(response.educations()).isNotEmpty();
      if (tc.expectedEducationInstitution != null) {
        assertThat(response.educations().getFirst().institution())
            .contains(tc.expectedEducationInstitution);
      }
    }
    if (tc.expectExperience) {
      assertThat(response.experiences()).isNotEmpty();
      if (tc.expectedExperienceCompany != null || tc.expectedExperienceHeader != null) {
        boolean matchedCompany = false;
        boolean matchedHeader = false;

        if (tc.expectedExperienceCompany != null) {
          matchedCompany =
              response.experiences().stream()
                  .anyMatch(
                      entry ->
                          entry.company() != null
                              && entry.company().contains(tc.expectedExperienceCompany));
          matchedHeader =
              response.experiences().stream()
                  .anyMatch(
                      entry ->
                          entry.headerLine() != null
                              && entry.headerLine().contains(tc.expectedExperienceCompany));
        }

        boolean matchedHeaderExpect =
            tc.expectedExperienceHeader != null
                && response.experiences().stream()
                    .anyMatch(
                        entry ->
                            entry.headerLine() != null
                                && entry.headerLine().contains(tc.expectedExperienceHeader));

        assertThat(matchedCompany || matchedHeader || matchedHeaderExpect).isTrue();
      }
    }
    if (tc.expectSkills) {
      assertThat(response.skills()).isNotEmpty();
    }
    if (tc.expectYears) {
      assertThat(response.yearsOfExperience()).isNotNull();
      if (tc.expectedYears != null) {
        assertThat(response.yearsOfExperience()).isEqualTo(tc.expectedYears);
      }
    }
    if (!tc.expectedSkills.isEmpty()) {
      assertThat(response.skills()).containsAll(tc.expectedSkills);
    }

    pipeline.close();
  }

  private record TemplateCase(
      String text,
      boolean expectEducation,
      boolean expectExperience,
      boolean expectSkills,
      boolean expectYears,
      Set<Skill> expectedSkills,
      String expectedEducationInstitution,
      String expectedExperienceCompany,
      String expectedExperienceHeader,
      Integer expectedYears) {}
}
