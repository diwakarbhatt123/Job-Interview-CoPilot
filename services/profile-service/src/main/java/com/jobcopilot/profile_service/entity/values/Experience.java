package com.jobcopilot.profile_service.entity.values;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;

@Builder(toBuilder = true)
public record Experience(
    String company,
    String role,
    LocalDate startAt,
    LocalDate endAt,
    boolean isCurrent,
    List<String> details) {
  public Experience {
    details = List.copyOf(details);
  }
}
