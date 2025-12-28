package com.jobcopilot.profile_service.repository;

import com.jobcopilot.profile_service.entity.values.Derived;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import java.time.Instant;

public interface ProfileSummaryView {
  String getId();

  String getDisplayName();

  ProfileStatus getStatus();

  Instant getCreatedAt();

  Instant getUpdatedAt();

  Derived getDerived();

  ResumeView getResume();

  interface ResumeView {
    ParsedView getParsed();
  }

  interface ParsedView {
    Integer getYearsOfExperience();
  }
}
