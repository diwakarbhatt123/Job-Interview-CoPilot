package com.jobcopilot.profile_service.repository;

import com.jobcopilot.profile_service.entity.values.Derived;
import com.jobcopilot.profile_service.enums.ProfileStatus;
import java.time.Instant;

public interface ProfileSummaryView {
  String id();

  String displayName();

  ProfileStatus status();

  Instant createdAt();

  Instant updatedAt();

  Derived derived();

  ResumeView resume();

  interface ResumeView {
    ParsedView parsed();
  }

  interface ParsedView {
    Integer yearsOfExperience();
  }
}
