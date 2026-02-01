package com.jobcopilot.parser.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ParsingUtils {

  public boolean isLikelyHeader(String line) {
    if (line == null) return false;

    String trimmed = line.trim();
    if (trimmed.isEmpty()) return false;

    // Reject bullets (common false positive inside experience details)
    if (startsWithBullet(trimmed)) return false;

    // Reject obvious sentence lines
    if (trimmed.endsWith(".")) return false;

    // Very long lines are unlikely to be headers
    if (trimmed.length() > 80) return false;

    // Strong signals first
    if (isAllCaps(trimmed) && wordCount(trimmed) <= 10) return true;

    // Colon is a decent signal, but avoid accepting any random line with ':'
    if (trimmed.endsWith(":") && wordCount(trimmed) <= 10) return true;

    // Title case is weaker; keep it conservative
    // Allow commas only if the line is short (avoid sentence-like lines)
    if (trimmed.contains(",") && wordCount(trimmed) > 6) return false;

    return looksLikeTitleCase(trimmed) && wordCount(trimmed) <= 6;
  }

  private boolean startsWithBullet(String s) {
    // normalize common bullet chars were already handled earlier, but keep this defensive
    return s.startsWith("-")
        || s.startsWith("•")
        || s.startsWith("▪")
        || s.startsWith("*")
        || s.startsWith("–")
        || s.startsWith("—");
  }

  private boolean isAllCaps(String line) {
    boolean hasLetter = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (Character.isLetter(c)) {
        hasLetter = true;
        if (!Character.isUpperCase(c)) return false;
      }
    }
    return hasLetter;
  }

  private int wordCount(String line) {
    String t = line.trim();
    return t.isEmpty() ? 0 : t.split("\\s+").length;
  }

  private boolean looksLikeTitleCase(String line) {
    String[] words = line.trim().split("\\s+");
    if (words.length == 0) return false;

    int titleCased = 0;
    int considered = 0;

    for (String w : words) {
      if (w.isEmpty()) continue;

      // Strip leading/trailing punctuation so "Experience:" and "(Skills)" still count.
      String cleaned = w.replaceAll("^[^\\p{L}]+|[^\\p{L}]+$", "");
      if (cleaned.isEmpty()) continue;

      considered++;
      char first = cleaned.charAt(0);
      if (Character.isUpperCase(first)) titleCased++;
    }

    if (considered == 0) return false;

    // Require majority title case, but not necessarily all words
    return titleCased >= Math.ceil(considered * 0.6);
  }
}
