package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection;
import com.jobcopilot.profile_service.parser.model.dictionary.Skill;
import com.jobcopilot.profile_service.parser.model.output.SectionizedOutput;
import com.jobcopilot.profile_service.parser.model.output.SkillExtractedOutput;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.util.CollectionUtils;

public class SkillExtractor implements PipelineStage {

  private static final Map<
          com.jobcopilot.profile_service.parser.model.dictionary.Skill, List<Pattern>>
      PATTERNS =
          compilePatterns(com.jobcopilot.profile_service.parser.model.dictionary.Skill.values());

  private static Set<Skill> matchAll(String text, boolean allowAmbiguous) {
    if (text == null || text.isBlank()) return Set.of();

    // already normalized by TextNormalizer; do not lowercase blindly
    String lower = text.toLowerCase(Locale.ROOT);

    Set<Skill> found = new HashSet<>();

    for (Map.Entry<Skill, List<Pattern>> entry : PATTERNS.entrySet()) {
      Skill canonical = entry.getKey();

      if (!allowAmbiguous && canonical.isAmbiguous()) {
        // Skip ambiguous canonical skills unless we are scanning SKILLS section
        continue;
      }

      for (Pattern p : entry.getValue()) {
        if (p.matcher(lower).find()) {
          found.add(canonical);
          break; // no need to match other aliases for the same canonical
        }
      }
    }

    return found;
  }

  private static String extractSectionText(SectionizedOutput sectionized) {
    if (CollectionUtils.isEmpty(sectionized.sections())) return "";

    return sectionized.sections().stream()
        .filter(d -> d.getSection() == ResumeSection.SKILLS)
        .flatMap(d -> d.getLines() == null ? Stream.empty() : d.getLines().stream())
        .collect(Collectors.joining("\n"));
  }

  private static Map<com.jobcopilot.profile_service.parser.model.dictionary.Skill, List<Pattern>>
      compilePatterns(com.jobcopilot.profile_service.parser.model.dictionary.Skill[] skills) {
    Map<com.jobcopilot.profile_service.parser.model.dictionary.Skill, List<Pattern>> out =
        new HashMap<>();
    Map<com.jobcopilot.profile_service.parser.model.dictionary.Skill, List<String>> dict =
        Arrays.stream(skills).collect(Collectors.toMap(skill -> skill, Skill::getAliases));
    for (Map.Entry<com.jobcopilot.profile_service.parser.model.dictionary.Skill, List<String>> e :
        dict.entrySet()) {
      Skill canonical = e.getKey();
      List<Pattern> patterns = new ArrayList<>();
      for (String alias : e.getValue()) {
        patterns.add(Pattern.compile(aliasToRegex(alias)));
      }
      out.put(canonical, patterns);
    }
    return Collections.unmodifiableMap(out);
  }

  /**
   * Converts an alias into a safe regex. Uses word boundaries where appropriate. Matches are
   * executed against lowercased text.
   */
  private static String aliasToRegex(String alias) {
    String a = alias.toLowerCase(Locale.ROOT).trim();

    // Special cases with punctuation / symbols
    // Add more if needed (c++, c#, .net, etc.)
    if (a.contains(".") || a.contains("+") || a.contains("#")) {
      // Escape regex metacharacters except spaces (we'll handle spaces below)
      return Pattern.quote(a).replace("\\ ", "\\s+");
    }

    // Regular word-ish aliases: enforce word boundaries
    // Convert spaces to \s+ to tolerate formatting
    String core = a.replaceAll("\\s+", "\\\\s+");
    return "\\b" + core + "\\b";
  }

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof SectionizedOutput sectionized)) {
      throw new IllegalArgumentException(
          "Invalid input type for SkillExtractor stage: " + input.getClass());
    }

    String normalizedText = sectionized.normalizedText();

    // 1) Build scan regions:
    //    - Prefer SKILLS section (high precision)
    //    - Also optionally scan whole document (for non-ambiguous skills)
    String skillsSectionText = extractSectionText(sectionized);
    String fullText = normalizedText == null ? "" : normalizedText;

    boolean hasSkillsSection = skillsSectionText != null && !skillsSectionText.isBlank();

    // 2) Extract skills from SKILLS section (if present)
    Set<Skill> extracted = new HashSet<>();
    if (hasSkillsSection) {
      extracted.addAll(matchAll(skillsSectionText, /* allowAmbiguous= */ true));
    }

    // 3) Fallback / supplemental scan across full doc:
    //    - Only for non-ambiguous skills (avoid "GO" false positives)
    extracted.addAll(matchAll(fullText, /* allowAmbiguous= */ false));

    // 4) Canonical, unique, sorted output
    List<Skill> skills =
        extracted.stream()
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());

    // If you want to store debug info (matched aliases, sources), add it to output later.
    return new SkillExtractedOutput(skills);
  }

  @Override
  public boolean isParallelizable() {
    return true;
  }
}
