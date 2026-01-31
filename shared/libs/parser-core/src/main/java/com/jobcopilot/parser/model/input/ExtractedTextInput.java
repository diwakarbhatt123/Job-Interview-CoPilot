package com.jobcopilot.parser.model.input;

import com.jobcopilot.parser.model.output.StageOutput;

public record ExtractedTextInput(String extractedText) implements StageInput, StageOutput {}
