package com.jobcopilot.profile_service.entity;

import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
public abstract class BaseEntity {
  @CreatedDate private Instant createdAt;
  @LastModifiedDate private Instant updatedAt;
}
