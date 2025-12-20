package com.jobcopilot.account_service.model.request;

import com.jobcopilot.account_service.constants.ValidationErrorMessage;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserLoginRequest(
    @NotBlank @Email(message = ValidationErrorMessage.INVALID_EMAIL) String email,
    @NotBlank @Length(min = 8, max = 64, message = ValidationErrorMessage.INVALID_PASSWORD_LENGTH)
        String password) {}
