package com.jobcopilot.job_analyzer_service.validator;

import com.jobcopilot.job_analyzer_service.enums.InputType;
import com.jobcopilot.job_analyzer_service.model.request.SubmitJobAnalysisRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SubmitJobAnalysisInputTypeValidator
    implements ConstraintValidator<InputTypeValidation, Object> {

  @Override
  public boolean isValid(Object value, ConstraintValidatorContext context) {
    if (value instanceof SubmitJobAnalysisRequest request) {
      if (InputType.URL == request.type()) {
        return request.url() != null && !request.url().trim().isEmpty();
      }
      if (InputType.PASTED == request.type()) {
        return request.text() != null && !request.text().trim().isEmpty();
      }
    }
    return false;
  }
}
