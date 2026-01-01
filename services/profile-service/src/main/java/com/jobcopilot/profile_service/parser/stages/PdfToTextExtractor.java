package com.jobcopilot.profile_service.parser.stages;

import com.jobcopilot.profile_service.parser.model.input.ExtractedTextInput;
import com.jobcopilot.profile_service.parser.model.input.StageInput;
import com.jobcopilot.profile_service.parser.model.output.StageOutput;
import com.jobcopilot.profile_service.parser.model.request.PDFAnalysisPipelineRequest;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.web.multipart.MultipartFile;

public class PdfToTextExtractor implements PipelineStage {

  @Override
  public StageOutput process(StageInput input) {
    if (!(input instanceof PDFAnalysisPipelineRequest(MultipartFile multipartFile))) {
      throw new IllegalArgumentException(
          "Unsupported input type for PdfToTextExtractor: " + input.getClass());
    }

    try (var inputStream = multipartFile.getInputStream();
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
