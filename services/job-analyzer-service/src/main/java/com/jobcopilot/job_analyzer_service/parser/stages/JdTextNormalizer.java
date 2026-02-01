package com.jobcopilot.job_analyzer_service.parser.stages;

import com.jobcopilot.job_analyzer_service.parser.model.output.NormalizedJdTextOutput;
import com.jobcopilot.job_analyzer_service.parser.model.request.JdAnalysisPipelineRequest;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.stages.PipelineStage;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class JdTextNormalizer implements PipelineStage {
  private static final Pattern MULTI_SPACE = Pattern.compile(" {2,}");
  private static final Pattern BULLETS = Pattern.compile("[•▪●–—*·>]+", Pattern.UNICODE_CASE);
  private static final Pattern DASHES = Pattern.compile("[−—–]+", Pattern.UNICODE_CASE);

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof JdAnalysisPipelineRequest(String rawText))) {
      throw new IllegalArgumentException("Expected JdAnalysisPipelineRequest");
    }
    if (rawText == null) {
      return new NormalizedJdTextOutput(null, "");
    }

    String normalized = rawText.replace("\r\n", "\n").replace("\r", "\n");
    normalized = Normalizer.normalize(normalized, Normalizer.Form.NFKC);

    List<String> normalizedLines = new ArrayList<>();
    for (String line : normalized.split("\n", -1)) {
      String current = line.replace("\t", " ");
      current = BULLETS.matcher(current).replaceAll("-");
      current = DASHES.matcher(current).replaceAll("-");
      current = MULTI_SPACE.matcher(current).replaceAll(" ");
      current = current.trim();
      normalizedLines.add(current);
    }

    return new NormalizedJdTextOutput(rawText, String.join("\n", normalizedLines));
  }
}
