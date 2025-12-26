package com.jobcopilot.profile_service.entity.values;

import lombok.Builder;

@Builder(toBuilder = true)
public record Resume(String rawText, Source source, Parsed parsed) {}
