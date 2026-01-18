package com.jobcopilot.job_analyzer_service.entity;

import com.jobcopilot.job_analyzer_service.entity.values.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
@Document(collection = "jobs")
@CompoundIndex(name = "userId_profileId_idx", def = "{'userId': 1, 'profileId': 1}")
@CompoundIndex(
    name = "analysis_status_lockedAt_idx",
    def = "{'analysis.status': 1, 'analysis.lockedAt': 1}")
@CompoundIndex(name = "profileId_input_url_idx", def = "{'profileId': 1, 'input.url': 1}")
public class Job extends BaseEntity {
  @Id private String id;
  private String userId;
  private String profileId;
  private Display display;
  private Input input;
  private Analysis analysis;
  private Extracted extracted;
  private Links links;
  private Versioning versioning;
}
