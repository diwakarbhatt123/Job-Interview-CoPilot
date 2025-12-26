package com.jobcopilot.profile_service.entity.values;

import java.time.LocalDate;
import lombok.Builder;

@Builder(toBuilder = true)
public record Award(
    String name, String description, String awardingAuthority, LocalDate awardedAt) {}
