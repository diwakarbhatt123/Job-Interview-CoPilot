package com.jobcopilot.account_service.dto;

import java.util.UUID;

public record AuthenticationResult(String token, UUID userId) {}
