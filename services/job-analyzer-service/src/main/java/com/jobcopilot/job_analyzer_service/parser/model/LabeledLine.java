package com.jobcopilot.job_analyzer_service.parser.model;

import com.jobcopilot.job_analyzer_service.parser.dictionary.BlockLabel;

public record LabeledLine(int lineIndex, String lineText, BlockLabel label) {}
