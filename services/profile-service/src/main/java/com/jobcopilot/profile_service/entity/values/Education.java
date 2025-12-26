package com.jobcopilot.profile_service.entity.values;

import lombok.Builder;

@Builder(toBuilder = true)
public record Education(String degree, String field, String institution) {}
