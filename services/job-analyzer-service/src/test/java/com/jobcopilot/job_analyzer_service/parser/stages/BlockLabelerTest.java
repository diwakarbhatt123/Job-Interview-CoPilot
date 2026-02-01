package com.jobcopilot.job_analyzer_service.parser.stages;

import static org.assertj.core.api.Assertions.assertThat;

import com.jobcopilot.job_analyzer_service.parser.dictionary.BlockLabel;
import com.jobcopilot.job_analyzer_service.parser.model.LabeledLine;
import java.util.List;
import org.junit.jupiter.api.Test;

class BlockLabelerTest {

  @Test
  void labelsLinesAfterHeaders() {
    String text =
        "Requirements\n"
            + "- Must have Java\n"
            + "Nice to have\n"
            + "- Docker\n"
            + "Responsibilities\n"
            + "- Build APIs\n";

    BlockLabeler labeler = new BlockLabeler();
    List<LabeledLine> lines = labeler.labelLines(text);

    assertThat(lines.get(1).label()).isEqualTo(BlockLabel.REQUIREMENTS);
    assertThat(lines.get(3).label()).isEqualTo(BlockLabel.PREFERRED);
    assertThat(lines.get(5).label()).isEqualTo(BlockLabel.RESPONSIBILITIES);
  }
}
