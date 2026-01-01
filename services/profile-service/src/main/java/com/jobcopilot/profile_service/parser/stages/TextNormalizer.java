package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.profile_service.parser.model.input.ExtractedTextInput;
import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.NormalizedTextOutput;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;
import com.jobcopilot.profile_service.parser.model.request.PlainTextAnalysisPipelineRequest;
import com.jobcopilot.profile_service.parser.utils.ParsingUtils;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextNormalizer implements PipelineStage {

  private static final Pattern MULTI_SPACE = Pattern.compile(" {2,}");
  private static final Pattern BULLETS = Pattern.compile("[•▪●–—*·>]+");
  private static final Pattern HYPHENS = Pattern.compile("[−—–]+");
  private static final Pattern NUMBERED_LIST = Pattern.compile("^[0-9]+[).].*");

  private static final List<UnaryOperator<String>> NORMALIZATION_STEPS =
      List.of(
          TextNormalizer::normalizeNewLines,
          TextNormalizer::normalizeUnicode,
          TextNormalizer::normalizeWhitespace,
          TextNormalizer::normalizeBullets,
          TextNormalizer::normalizeHyphensAndDashes,
          TextNormalizer::repairLineWrapping,
          TextNormalizer::cleanUpHeaders);

  @Override
  public StageOutput process(StageInput input) {
    String rawText = getRawText(input);
    String finalText =
        NORMALIZATION_STEPS.stream()
            .reduce(rawText, (text, op) -> op.apply(text), (_, right) -> right);
    return new NormalizedTextOutput(finalText, rawText);
  }

  private String getRawText(StageInput input) {
    if (input instanceof PlainTextAnalysisPipelineRequest(String plainText)) {
      return plainText;
    } else if (input instanceof ExtractedTextInput(String extractedText)) {
      return extractedText;
    } else {
      throw new IllegalArgumentException(
          "Unsupported input type for TextNormalizer: " + input.getClass());
    }
  }

  private static String normalizeNewLines(String text) {
    return text.replace("\r\n", "\n").replace("\r", "\n");
  }

  private static String normalizeUnicode(String text) {
    return Normalizer.normalize(text, Normalizer.Form.NFKC);
  }

  private static String normalizeWhitespace(String text) {
    return text.replace('\t', ' ')
        .lines()
        .map(
            line -> {
              String singleSpaced = MULTI_SPACE.matcher(line).replaceAll(" ");
              return singleSpaced.strip();
            })
        .collect(Collectors.joining("\n"));
  }

  private static String normalizeBullets(String text) {
    return BULLETS.matcher(text).replaceAll("-");
  }

  private static String normalizeHyphensAndDashes(String text) {
    return HYPHENS.matcher(text).replaceAll("-");
  }

  private static String repairLineWrapping(String text) {
    List<String> lines = text.lines().toList();
    if (lines.isEmpty()) {
      return "";
    }

    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder(lines.getFirst());

    for (int i = 1; i < lines.size(); i++) {
      String nextLine = lines.get(i);

      if (shouldMerge(current.toString(), nextLine)) {
        current.append(" ").append(nextLine.trim());
      } else {
        result.add(current.toString());
        current = new StringBuilder(nextLine);
      }
    }

    result.add(current.toString());
    return String.join("\n", result);
  }

  private static String cleanUpHeaders(String text) {
    return text.lines()
        .map(
            line -> {
              if (ParsingUtils.isLikelyHeader(line)) {
                return line.replaceAll(":+", "").replaceAll("-+", "").trim();
              } else {
                return line;
              }
            })
        .collect(Collectors.joining("\n"));
  }

  private static boolean shouldMerge(String previous, String next) {
    String prev = previous.trim();
    String nxt = next.trim();

    if (prev.isEmpty() || nxt.isEmpty()) {
      return false;
    }
    if (ParsingUtils.isLikelyHeader(prev)) {
      return false;
    }
    if (endsWithPunctuation(prev)) {
      return false;
    }
    if (!startsWithLowercaseLetter(nxt)) {
      return false;
    }
    if (ParsingUtils.isLikelyHeader(nxt)) {
      return false;
    }
    return !looksLikeBulletOrListItem(prev) && !looksLikeBulletOrListItem(nxt);
  }

  private static boolean endsWithPunctuation(String line) {
    if (line.isEmpty()) return false;
    char last = line.charAt(line.length() - 1);
    return ".?!:;,".indexOf(last) >= 0;
  }

  private static boolean startsWithLowercaseLetter(String line) {
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (Character.isLetter(c)) {
        return Character.isLowerCase(c);
      }
    }
    return false;
  }

  private static boolean looksLikeBulletOrListItem(String line) {
    String trimmed = line.trim();
    if (trimmed.isEmpty()) return false;

    if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
      return true;
    }

    return NUMBERED_LIST.matcher(trimmed).matches();
  }
}
