package com.jobcopilot.profile_service.parser.model.request;

import java.util.Arrays;

public record PDFAnalysisPipelineRequest(byte[] pdfBytes, String filename, String contentType)
    implements PipelineRequest {
  public PDFAnalysisPipelineRequest {
    pdfBytes = (pdfBytes == null) ? null : Arrays.copyOf(pdfBytes, pdfBytes.length);
  }

  @Override
  public byte[] pdfBytes() {
    return (pdfBytes == null) ? null : Arrays.copyOf(pdfBytes, pdfBytes.length);
  }
}
