package com.jobcopilot.profile_service.entity.values;

import com.jobcopilot.profile_service.enums.SourceType;
import java.time.Instant;
import lombok.Builder;

@Builder(toBuilder = true)
public record Source(SourceType type, String fileName, String contentType, Instant uploadedAt) {}
