package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.parser.model.input.ExtractedTextInput;
import com.jobcopilot.parser.model.input.StageInput;
import com.jobcopilot.parser.model.output.StageOutput;
import com.jobcopilot.parser.model.request.PDFAnalysisPipelineRequest;
import com.jobcopilot.parser.stages.PipelineStage;
import java.io.ByteArrayInputStream;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

public class PdfToTextExtractor implements PipelineStage {

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof PDFAnalysisPipelineRequest(byte[] pdfBytes, _, _))) {
      throw new IllegalArgumentException(
          "Unsupported input type for PdfToTextExtractor: " + input.getClass());
    }

    if (pdfBytes == null || pdfBytes.length == 0) {
      throw new IllegalArgumentException("PDF bytes are empty");
    }

    try (var inputStream = new ByteArrayInputStream(pdfBytes);
        PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream))) {
      PDFTextStripper stripper = new PDFTextStripper();
      stripper.setSortByPosition(true);
      String extractedText = stripper.getText(document);

      return new ExtractedTextInput(extractedText);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
