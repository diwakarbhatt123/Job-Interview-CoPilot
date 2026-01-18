package com.jobcopilot.job_analyzer_service.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = SubmitJobAnalysisInputTypeValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface InputTypeValidation {
  String message() default "Invalid input for request type.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
