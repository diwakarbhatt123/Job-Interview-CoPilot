package com.jobcopilot.profile_service.entity;

import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

public abstract class BaseEntity {
  @CreatedDate private Instant createdAt;
  @LastModifiedDate private Instant updatedAt;
}
