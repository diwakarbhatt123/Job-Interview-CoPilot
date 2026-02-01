package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import com.jobcopilot.parser.utils.ParsingUtils;
import java.util.ArrayList;
import java.util.List;

public class Sectionizer implements PipelineStage {
  private static String normalizeHeaderForAlias(String header) {
    String s = header.trim();

    while (s.endsWith(":")) {
      s = s.substring(0, s.length() - 1).trim();
    }
    s = s.replaceAll("[-–—]+$", "").trim();

    s = s.replaceAll("\\s+", " ");
    return s.toLowerCase();
  }

  @Override
  public StageOutput process(StageInput input) {

    if (!(input
        instanceof
        com.jobcopilot.profile_service.parser.model.output.NormalizedTextOutput(
            String inputText,
            String rawText))) {
      throw new IllegalArgumentException(
          "Invalid input type for Sectionizer stage: " + input.getClass());
    }

    List<String> lines = inputText.lines().toList();
    List<com.jobcopilot.profile_service.parser.model.output.SectionizedOutput.SectionDetail>
        sectionDetails = new ArrayList<>();

    int i = 0;
    while (i < lines.size()) {
      String currentRaw = lines.get(i);
      if (!ParsingUtils.isLikelyHeader(currentRaw)) {
        i++;
        continue;
      }

      String headerName = currentRaw.trim();
      String headerForAlias = normalizeHeaderForAlias(headerName);
      com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection section =
          com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection.fromAliasOrUnknown(
              headerForAlias);
      if (section == com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection.UNKNOWN) {
        i++;
        continue;
      }

      int startLine = i;
      List<String> sectionLines = new ArrayList<>();

      int j = i + 1;
      int endLine = lines.size() - 1;

      while (j < lines.size()) {
        String nextRaw = lines.get(j);

        if (ParsingUtils.isLikelyHeader(nextRaw)) {
          String nextHeaderForAlias = normalizeHeaderForAlias(nextRaw);
          com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection nextSection =
              com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection
                  .fromAliasOrUnknown(nextHeaderForAlias);
          if (nextSection
              != com.jobcopilot.profile_service.parser.model.dictionary.ResumeSection.UNKNOWN) {
            endLine = j - 1;
            break;
          }
        }

        sectionLines.add(nextRaw);
        j++;
      }

      com.jobcopilot.profile_service.parser.model.output.SectionizedOutput.SectionDetail detail =
          com.jobcopilot.profile_service.parser.model.output.SectionizedOutput.SectionDetail
              .builder()
              .name(headerName)
              .startLine(startLine)
              .endLine(endLine)
              .section(section)
              .lines(sectionLines)
              .build();

      sectionDetails.add(detail);

      i = Math.min(j, lines.size());
    }

    return new com.jobcopilot.profile_service.parser.model.output.SectionizedOutput(
        rawText, inputText, sectionDetails);
  }
}
